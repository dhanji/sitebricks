package org.sitebricks.client.easy.service;

import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Patch;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;

import org.sitebricks.client.easy.Bar;
import org.sitebricks.client.easy.Foo;

import java.util.Collection;

import javax.inject.Inject;

@At("/service/foo")
@Service
@javax.inject.Named()
public class FooService {

  @Inject
  private Foo foo;

  @Get
  public Reply<?> get() {
    final Collection<Bar> bars = foo.get();
    return Reply.with(bars).type("text/json").as(Json.class);
  }

  @Get
  @At("/:id")
  public Reply<?> get(@Named("id") final String id) {
    final Bar bar = foo.get(id);
    return Reply.with(bar).type("text/json").as(Json.class);
  }

  @Put
  public Reply<?> add(final Request submission) {
    final Bar bar = submission.read(Bar.class).as(Json.class);

    foo.add(bar);

    return Reply.saying().ok();
  }

  @Patch
  public Reply<?> update(final Request submission) {
    final Bar bar = submission.read(Bar.class).as(Json.class);

    foo.update(bar);

    return Reply.saying().ok();
  }

  @Delete
  @At("/:id")
  public Reply<?> remove(@Named("id") final String id) {
    foo.remove(id);
    return Reply.saying().ok();
  }

  @Get
  @At("/:echo/:message")
  public Reply<?> echo(@Named("echo") final String echo, @Named("message") final String message) {
    return Reply.with(echo + " says " + message + "!").as(Json.class);
  }

  @Post
  public Reply<?> echo(final Request submission) {
    final String message = submission.read(String.class).as(Json.class);
    return Reply.with("Sitebricks says " + message + "!").as(Json.class);
  }

}
