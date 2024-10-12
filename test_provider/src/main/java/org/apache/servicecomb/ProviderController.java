package org.apache.servicecomb;

import org.apache.servicecomb.config.DynamicProperties;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.annotation.Autowired;

@RestSchema(schemaId = "ProviderController", schemaInterface = ProviderService.class)
public class ProviderController implements ProviderService {

  @Override
  public String sayHello(String name) {
    return "Hello " + name;
  }

}