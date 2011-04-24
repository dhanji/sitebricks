package info.sitebricks.persist;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import info.sitebricks.data.Document;
import info.sitebricks.data.Index;
import info.sitebricks.data.Index.Node;

import java.util.Date;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class WikiStore {
  private final Provider<ObjectContainer> container;

  @Inject
  public WikiStore(Provider<ObjectContainer> container) {
    this.container = container;
  }

  public void store(Document document) {
    container.get().store(document);
  }

  // Names are unique.
  public Document fetch(final String name) {
    ObjectSet<Document> set = container.get().query(new Predicate<Document>() {
      @Override
      public boolean match(Document candidate) {
        return name.equals(candidate.getName());
      }
    });
    return set.isEmpty() ? null : set.next();
  }

  // TODO Cache index in memory?
  public Index fetchIndex() {
    ObjectContainer objects = container.get();
    ObjectSet<Index> set = objects.query(Index.class);

    // Create the index if none exists (first time ever)-- not threadsafe!!
    if (set.isEmpty()) {
      Document home = new Document();
      home.setTopic(Document.HOME);  // implicitly sets the doc name
      home.setAuthor("dhanji");
      home.setCreatedOn(new Date());
      home.setText("");
      objects.store(home);

      Node root = new Node();
      root.setDocument(home);
      Index index = new Index(root);
      index.list().add(root);

      objects.store(index);
      return index;
    }

    return set.next();
  }
}
