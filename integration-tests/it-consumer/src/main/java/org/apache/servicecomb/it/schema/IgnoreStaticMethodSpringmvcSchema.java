package org.apache.servicecomb.it.schema;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "ignoreStaticMethodSpringmvcSchema")
@RequestMapping(path = "/ignoreStaticMethodSpringmvcSchema")
public class IgnoreStaticMethodSpringmvcSchema {

  private IgnoreStaticMethodSpringmvcSchema() {
  }

  private static IgnoreStaticMethodSpringmvcSchema INSTANCE = new IgnoreStaticMethodSpringmvcSchema();

  public static IgnoreStaticMethodSpringmvcSchema getINSTANCE() {
    return INSTANCE;
  }

  @GetMapping("staticSub")
  public static int staticSub(int num1, int num2) {
    return num1 - num2;
  }

  @GetMapping("add")
  public int add(int num1, int num2) {
    return num1 + num2;
  }
}
