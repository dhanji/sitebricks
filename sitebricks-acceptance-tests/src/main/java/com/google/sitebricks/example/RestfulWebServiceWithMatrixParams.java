package com.google.sitebricks.example;

import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

/**
 * Demonstrates subpaths in a restful webservice.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/matrixpath") @Service
public class RestfulWebServiceWithMatrixParams {
  public static final String TOPLEVEL = "toplevel_m";
  public static final String PATH_1 = "path1";

  @Get
  public Reply<?> topLevel() {
    return Reply.with(TOPLEVEL);
  }

  @At("/:variable/:id") @Post
  public Reply<String> variableSecondLevel(@Named("variable") String arg, @Named("id") String id,
                                           Request request) {
    return Reply.with(request.matrix().toString() + "_" + arg + "_" + id);
  }
}
