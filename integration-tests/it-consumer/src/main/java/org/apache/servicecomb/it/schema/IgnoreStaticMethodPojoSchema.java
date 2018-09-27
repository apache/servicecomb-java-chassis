package org.apache.servicecomb.it.schema;

import org.apache.servicecomb.provider.pojo.RpcSchema;

import io.swagger.annotations.SwaggerDefinition;

@RpcSchema(schemaId = "ignoreStaticMethodPojoSchema")
@SwaggerDefinition(basePath = "/ignoreStaticMethodPojoSchema")
public class IgnoreStaticMethodPojoSchema {

  private IgnoreStaticMethodPojoSchema() {
  }

  private static IgnoreStaticMethodPojoSchema INSTANCE = new IgnoreStaticMethodPojoSchema();

  public static IgnoreStaticMethodPojoSchema getINSTANCE() {
    return INSTANCE;
  }

  public static int staticSub(int num1, int num2) {
    return num1 - num2;
  }

  public int add(int num1, int num2) {
    return num1 + num2;
  }
}
