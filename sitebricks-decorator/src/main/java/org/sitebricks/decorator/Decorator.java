package org.sitebricks.decorator;

import java.io.File;
import java.io.Writer;
import java.util.Map;

/**
 * @author Jason van Zyl
 */
public interface Decorator {
  void decorate(File decoratorSource, Map<String, Object> context, Writer writer);
}