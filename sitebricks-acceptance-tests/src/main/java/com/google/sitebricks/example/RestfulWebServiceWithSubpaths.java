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
@At("/superpath") @Service
public class RestfulWebServiceWithSubpaths {
  public static final String TOPLEVEL = "toplevel";
  public static final String PATH_1 = "path1";
  public static final String PATH_2 = "path2";
  public static final String PATH_3 = "path3";

  @Get
  public Reply<?> topLevel() {
    return Reply.with(TOPLEVEL);
  }

  @At("/subpath1") @Post
  public Reply<String> path1() {
    return Reply.with(PATH_1);
  }

  @At("/subpath2") @Post
  public Reply<String> path2() {
    return Reply.with(PATH_2);
  }

  @At("/subpath3") @Post
  public Reply<String> path3() {
    return Reply.with(PATH_3);
  }

  @At("/subpath1/:variable") @Post
  public Reply<String> variable(@Named("variable") String arg) {
    return Reply.with(arg);
  }

  @At("/subpath1/:variable/:id") @Post
  public Reply<String> variableSecondLevel(@Named("variable") String arg, @Named("id") String id) {
    return Reply.with(arg + "_" + id);
  }

  @At("/subpath3/:sec") @Post
  public Reply<String> variableSubpath2(@Named("sec") String arg) {
    return Reply.with(arg);
  }
}
