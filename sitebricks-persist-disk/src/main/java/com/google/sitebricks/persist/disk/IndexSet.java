package com.google.sitebricks.persist.disk;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class IndexSet {
  private static final Logger log = Logger.getLogger(IndexSet.class.getName());
  private final String dataDir;

  IndexSet(String dataDir) {
    this.dataDir = dataDir;
  }

  static class LuceneIndex {
    volatile DirectoryReader directory;
    final IndexWriter writer;
    final AtomicReference<IndexSearcher> searcher;

    public LuceneIndex(DirectoryReader directory, IndexWriter writer, IndexSearcher searcher) {
      this.directory = directory;
      this.writer = writer;
      this.searcher = new AtomicReference<IndexSearcher>(searcher);
    }
  }

  private final StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);

  // Lazy-evaluative concurrent map of index names -> indexes
  private LuceneIndex index;

  public synchronized void startup() {
    String name = "sb_disk_store";
    try {
      File dir = new File(dataDir + "/" + name);
      NIOFSDirectory directory = new NIOFSDirectory(dir);

      IndexWriter writer;
      IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_40, analyzer);
      LogByteSizeMergePolicy mergePolicy = new LogByteSizeMergePolicy();
      mergePolicy.setMaxMergeMB(50.0);
      mergePolicy.setUseCompoundFile(true);
      mergePolicy.setMergeFactor(8);
      writerConfig.setMergePolicy(mergePolicy);
      writerConfig.setRAMBufferSizeMB(128.0);
      writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

      writer = new IndexWriter(directory, writerConfig);

      DirectoryReader reader = DirectoryReader.open(writer, true);
      this.index = new LuceneIndex(reader, writer, new IndexSearcher(reader));

    } catch (IOException e) {
      log.log(Level.SEVERE, "Unable to create index for " + name, e);
      throw new RuntimeException(e);
    }

  }

  public void shutdown() {
      try {
        if (index.writer != null)
          index.writer.close();
      } catch (IOException e) {
        log.log(Level.SEVERE, "Error closing search disk index", e);
      }
      try {
        if (index.searcher.get() != null)
          index.searcher.get().getIndexReader().close();
      } catch (IOException e) {
        log.log(Level.SEVERE, "Error closing search disk index", e);
      }
      try {
        if (index.directory != null)
          index.directory.close();
      } catch (IOException e) {
        log.log(Level.SEVERE, "Error closing search directory index", e);
      }
  }

  // This has pool semantics, so consider making this a checkin/checkout style operation.
  public LuceneIndex current() {
    // Reopen index searcher if necessary.
    IndexSearcher oldSearcher = index.searcher.get();

    try {
      // Guarded lock, to prevent us from unnecessarily synchronizing every time.
      if (oldSearcher == null) {
        synchronized (this) {
          if ((oldSearcher = index.searcher.get()) == null) {
            IndexSearcher newSearcher = new IndexSearcher(index.directory);
            index.searcher.compareAndSet(null, newSearcher);
          }
        }

        // Yes it's open, but is it stale?
      } else {
        DirectoryReader directoryReader = DirectoryReader.openIfChanged(index.directory);
        if (null != directoryReader) {
          index.directory = directoryReader;

          synchronized (this) {
            IndexSearcher newSearcher;
            try {
              newSearcher = new IndexSearcher(directoryReader);
            } catch (AlreadyClosedException e) {
              log.warning("Old index reader could not be reopened. Opening a new one...");
              newSearcher = new IndexSearcher(DirectoryReader.open(index.writer, true));
            }
            if (!index.searcher.compareAndSet(oldSearcher, newSearcher)) {
              // Someone beat us to it. No worries.
              log.finest("Another searcher was already open for this index. Nothing to do.");
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return index;
  }

  public void delete() {
    try {
      IndexWriter writer = index.writer;

      writer.deleteAll();
      writer.commit();
    } catch (IOException e) {
      log.log(Level.SEVERE, "IO error while deleting index.", e);
    }
  }

  public StandardAnalyzer getAnalyzer() {
    return analyzer;
  }
}
