package org.apache.servicecomb.it.schema;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "ignoreStaticMethodJaxrsSchema")
@Path("/ignoreStaticMethodJaxrsSchema")
public class IgnoreStaticMethodJaxrsSchema {

  private IgnoreStaticMethodJaxrsSchema() {
  }

  private static IgnoreStaticMethodJaxrsSchema INSTANCE = new IgnoreStaticMethodJaxrsSchema();

  public static IgnoreStaticMethodJaxrsSchema getINSTANCE() {
    return INSTANCE;
  }

  @GET
  @Path("staticSub")
  public static int staticSub(@QueryParam("num1") int num1, @QueryParam("num2") int num2) {
    return num1 - num2;
  }

  @GET
  @Path("add")
  public int add(@QueryParam("num1") int num1, @QueryParam("num2") int num2) {
    return num1 + num2;
  }
}
