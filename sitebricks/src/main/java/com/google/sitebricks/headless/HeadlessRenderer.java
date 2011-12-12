package com.google.sitebricks.headless;

import com.google.inject.ImplementedBy;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A utility that populates a servlet response with the data from a headless
 * web service reply (typically a Reply<E> object returned from a @Get (or
 * similar) HTTP method on a @Service annotated class.
 */
@ImplementedBy(ReplyBasedHeadlessRenderer.class)
public interface HeadlessRenderer {
  void render(HttpServletResponse response, Object o) throws IOException;
}
