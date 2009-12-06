package com.google.sitebricks.example;


import com.google.sitebricks.At;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import com.google.sitebricks.http.Select;

import java.util.ArrayList;
import java.util.List;


@At("/select") @Select("event")
public class SelectRouting {

  private List<String> data = new ArrayList<String>();

  public SelectRouting() {
  }

  public SelectRouting(List<String> data) {
    this.data = data;
  }

  public List<String> getData() {
    return data;
  }

  public void setData(List<String> data) {
    this.data = data;
  }

  @Post
  public void defaultPost() {
    data.add("defaultPost");
  }

  @Post("foo")
  public void fooPost() {
    data.add("fooPost");
  }

  @Post("bar")
  public void barPost() {
    data.add("barPost");
  }

  @Post("304")
  public Object redirectPost() {
    data.add("redirectPost");
    return new SelectRouting(data);
  }

  @Get
  public void defaultGet() {
    data.add("defaultGet");
  }

  @Get("foo")
  public void fooGet() {
    data.add("fooGet");
  }

  @Get("bar")
  public void barGet() {
    data.add("barGet");
  }

  @Get("304")
  public Object redirectGet() {
    data.add("redirectGet");
    return new SelectRouting(data);
  }

  @Put
  public void defaultPut() {
    data.add("defaultPut");
  }

  @Put("foo")
  public void fooPut() {
    data.add("fooPut");
  }

  @Put("bar")
  public void barPut() {
    data.add("barPut");
  }

  @Put("304")
  public Object redirectPut() {
    data.add("redirectPut");
    return new SelectRouting(data);
  }

  @Delete
  public void defaultDelete() {
    data.add("defaultDelete");
  }

  @Delete("foo")
  public void fooDelete() {
    data.add("fooDelete");
  }

  @Delete("bar")
  public void barDelete() {
    data.add("barDelete");
  }

  @Delete("304")
  public Object redirectDelete() {
    data.add("redirectDelete");
    return new SelectRouting(data);
  }
}
