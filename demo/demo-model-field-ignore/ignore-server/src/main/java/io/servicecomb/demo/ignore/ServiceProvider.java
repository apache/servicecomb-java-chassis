package io.servicecomb.demo.ignore;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "ignoreServiceProvider")
@RequestMapping("/")
@Controller
public class ServiceProvider implements Service {

  @RequestMapping(method = RequestMethod.POST, value = "/")
  @ResponseBody
  public OutputModelForTestIgnore test(@RequestBody InputModelForTestIgnore input) {
    return new OutputModelForTestIgnore("output_id", input.getInputId(), input.getContent());
  }
}