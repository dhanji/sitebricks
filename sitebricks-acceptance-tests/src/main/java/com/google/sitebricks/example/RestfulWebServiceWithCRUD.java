package com.google.sitebricks.example;

import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Patch;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;

/**
 * Demonstrates CRUD operations in a restful webservice.
 * 
 * // ------------------------------
 * // Method   URL        Action
 * // ------------------------------
 * // POST     /user     CREATE
 * // GET      /user     READ (collection)
 * // GET      /user/1   READ (individual)
 * // PUT      /user/1   UPDATE
 * // DELETE   /user/1   DELETE
 *
 * @author Jason van Zyl
 */
@At("/json/:type") @Service
public class RestfulWebServiceWithCRUD {
	public static final String TYPE = "user";
  public static final String BASE_SERVICE_PATH = "/json/" + TYPE;
  public static final String CREATE = "CREATE";
  public static final String READ_COLLECTION = "READ_COLLECTION";
  public static final String READ_INDIVIDUAL = "READ_INDIVIDUAL";
  public static final String UPDATE = "UPDATE";
  public static final String PARTIAL_UPDATE = "PARTIAL_UPDATE";
  public static final String DELETE = "DELETE";

  @Post
  public Reply<?> post( Request<String> request, @Named( "type" ) String type ) { 
    return Reply.with(CREATE);
  }

  @Get
  public Reply<?> get( @Named( "type" ) String type ) {
    return Reply.with(READ_COLLECTION);
  }

  @At( "/:id" ) @Get
  public Reply<?> get( @Named( "type" ) String type, @Named( "id" ) String id ) {
    return Reply.with(READ_INDIVIDUAL);
  }

  @At( "/:id" ) @Put
  public Reply<?> put( Request<String> request, @Named( "type" ) String type, @Named( "id" ) String id ) {
    return Reply.with(UPDATE);
  }

  @At( "/:id" ) @Patch
  public Reply<?> patch( Request<String> request, @Named( "type" ) String type, @Named( "id" ) String id ) {
    return Reply.with(PARTIAL_UPDATE);
  }

  @At( "/:id" ) @Delete
  public Reply<?> delete( @Named( "type" ) String type, @Named( "id" ) String id ) {
    return Reply.with(DELETE);
  }  
}
