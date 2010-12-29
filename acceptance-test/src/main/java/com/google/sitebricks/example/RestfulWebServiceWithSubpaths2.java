package com.google.sitebricks.example;

import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

/**
 * Demonstrates subpaths in a restful webservice.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/superpath2/:dynamic") @Service
public class RestfulWebServiceWithSubpaths2 {
  public static final String TOPLEVEL = "test1";
  public static final String PATH_1 = "path1";

  @Get
  public Reply<?> topLevel(@Named("dynamic") String dynamic) {
    return Reply.with(dynamic);
  }

  @At("/subpath1") @Post
  public Reply<String> path1(@Named("dynamic") String dynamic) {
    return Reply.with(PATH_1);
  }

  @At("/:second") @Post
  public Reply<String> path2(@Named("dynamic") String dynamic, @Named("second") String second) {
    return Reply.with(dynamic + "_" + second);
  }
}
