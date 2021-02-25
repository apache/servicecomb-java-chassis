package org.apache.servicecomb.demo.springmvc.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annatation.RequestMapping;
import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "testFileSource")
@RequestMapping(path = "/springmvc/fileSource")
public class ReadFileSource {
  @Value("${int:-1}")
  public int testInt;

  @GetMapping(path = "/int")
  public int getTestInt() {
    return testInt;
  }

  @Value("${String:error}")
  public String testString;

  @GetMapping(path = "/String")
  public String getTestString() {
    return testString;
  }
}
