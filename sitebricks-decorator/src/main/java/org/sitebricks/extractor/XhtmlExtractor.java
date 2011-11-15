package org.sitebricks.extractor;

import java.io.File;
import java.io.IOException;

public interface XhtmlExtractor {
  public ExtractResult extract(String xhtml);
  public ExtractResult extract(File xhtml) throws IOException;
}
