
package org.apache.servicecomb;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(path = "/provider")
public interface ProviderService {

  @GetMapping("/sayHello")
  String sayHello(@RequestParam("name") String name);
}