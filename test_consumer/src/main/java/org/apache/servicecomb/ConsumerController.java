package org.apache.servicecomb;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.annotation.Autowired;

@RestSchema(schemaId = "ConsumerController", schemaInterface = ConsumerService.class)
public class ConsumerController implements ConsumerService {

  private ProviderService providerService;

  @Autowired
  public void setProviderService(ProviderService providerService) {
    this.providerService = providerService;
  }

  @Override
  public String sayHello(String name) {
    return providerService.sayHello(name);
  }
}