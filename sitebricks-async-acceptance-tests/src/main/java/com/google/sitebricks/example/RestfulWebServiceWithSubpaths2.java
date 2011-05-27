package com.google.sitebricks.example;

import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;

/**
 * Demonstrates subpaths in a restful webservice.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/superpath2/:dynamic") @Service
public class RestfulWebServiceWithSubpaths2 {
  public static final String TOPLEVEL = "test1";
  public static final String PATH_1 = "path1";
  public static final String PATH_1_PUT = "path1put";
  public static final String PATH_1_DELETE = "path1delete";

  @Get
  public Reply<?> topLevel(@Named("dynamic") String dynamic) {
    return Reply.with(dynamic);
  }

  @At("/subpath1") @Post
  public Reply<String> path1(@Named("dynamic") String dynamic) {
    return Reply.with(PATH_1);
  }

  @At("/subpath1") @Put
  public Reply<String> path1Put(@Named("dynamic") String dynamic) {
    return Reply.with(PATH_1_PUT);
  }

  @At("/subpath1") @Delete
  public Reply<String> path1Delete(@Named("dynamic") String dynamic) {
    return Reply.with(PATH_1_DELETE);
  }

  @At("/:second") @Post
  public Reply<String> path2(@Named("dynamic") String dynamic, @Named("second") String second) {
    return Reply.with(dynamic + "_" + second);
  }

  @At("/:second") @Delete
  public Reply<String> path2Delete(@Named("dynamic") String dynamic,
                                   @Named("second") String second) {
    return Reply.with("delete:" + dynamic + "_" + second);
  }

  @At("/:l2/:l3") @Delete
  public Reply<String> l2l3(@Named("dynamic") String dynamic,
                            @Named("l2") String second,
                            @Named("l3") String third) {
    return Reply.with("delete:" + dynamic + "_" + second + "_" + third);
  }

  @At("/:l2/:l3") @Put
  public Reply<String> l2l3Put(@Named("dynamic") String dynamic,
                            @Named("l2") String second,
                            @Named("l3") String third) {
    return Reply.with("put:" + dynamic + "_" + second + "_" + third);
  }

  @At("/:l2/:l3") @Post
  public Reply<String> l2l3Post(@Named("dynamic") String dynamic,
                            @Named("l2") String second,
                            @Named("l3") String third) {
    return Reply.with("post:" + dynamic + "_" + second + "_" + third);
  }

  @At("/:l2/:l3") @Get
  public Reply<String> l2l3Get(@Named("dynamic") String dynamic,
                            @Named("l2") String second,
                            @Named("l3") String third) {
    return Reply.with("get:" + dynamic + "_" + second + "_" + third);
  }

// BUG EXISTS WHEN 4-Level MIXED PATHS ARE INTRODUCED:

  @At("/:l2/:l3/l4") @Get
  public Reply<String> l4Get(@Named("dynamic") String dynamic,
                            @Named("l2") String second,
                            @Named("l3") String third) {
    return Reply.with("4l:get:" + dynamic + "_" + second + "_" + third);
  }

  @At("/:l2/:l3/l4") @Post
  public Reply<String> l4Post(@Named("dynamic") String dynamic,
                            @Named("l2") String second,
                            @Named("l3") String third) {
    return Reply.with("4l:post:" + dynamic + "_" + second + "_" + third);
  }
}
