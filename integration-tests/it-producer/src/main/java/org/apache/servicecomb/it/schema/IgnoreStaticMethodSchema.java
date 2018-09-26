package org.apache.servicecomb.it.schema;

import org.apache.servicecomb.provider.pojo.RpcSchema;

import io.swagger.annotations.SwaggerDefinition;

@RpcSchema(schemaId = "ignoreStaticMethodSchema")
@SwaggerDefinition(basePath = "/v1/ignoreStaticMethodSchema")
public class IgnoreStaticMethodSchema {

  private IgnoreStaticMethodSchema() {
  }

  private static IgnoreStaticMethodSchema INSTANCE = new IgnoreStaticMethodSchema();

  public static IgnoreStaticMethodSchema getINSTANCE() {
    return INSTANCE;
  }

  public static int sub(int num1, int num2) {
    return num1 - num2;
  }

  public int add(int num1, int num2) {
    return num1 + num2;
  }

}
