package org.apache.servicecomb.demo.springmvc.server;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "codeFirstBizkeeperTest")
@RequestMapping(path = "/codeFirstBizkeeperTest", produces = MediaType.APPLICATION_JSON_VALUE)
public class BizkeeperTest {
  @GetMapping(path = "/testTimeout")
  public String testTimeout(@RequestParam("name") String name, @RequestParam("delaytime") long delaytime) {
    try {
      Thread.sleep(delaytime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return name;
  }
}
