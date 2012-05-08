package org.sitebricks.client.easy;

import com.google.sitebricks.At;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Patch;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;

import java.util.Collection;

import javax.inject.Named;

@At("/service/foo")
public interface Foo {
  @Put
  void add(Bar bar);

  @Patch
  void update(Bar bar);

  @Delete
  @At("/:id")
  void remove(@Named("id") String id);

  @Get
  Collection<Bar> get();

  @Get
  @At("/:id")
  Bar get(@Named("id") String id);

  void missingAnnotation();

  @Get
  @At("/:id")
  void missingBinding();

  @Get
  @At("/:echo/:message")
  String echo(@Named("message") String message);

  @Post
  String echoWithPost(String message);
}
