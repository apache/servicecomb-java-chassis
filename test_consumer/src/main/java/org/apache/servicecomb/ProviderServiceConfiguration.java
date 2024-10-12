package org.apache.servicecomb;

import org.apache.servicecomb.provider.pojo.Invoker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderServiceConfiguration {
  @Bean
  public ProviderService providerService() {
    return Invoker.createProxy("provider", "ProviderController", ProviderService.class);
  }
}