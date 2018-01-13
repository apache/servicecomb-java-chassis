package org.apache.servicecomb.springboot.starter.discovery;

import org.junit.Assert;
import org.junit.Test;

public class TestCseDiscoveryClientConfiguration {
  @Test
  public void testCseDiscoveryClientConfiguration() {
    CseDiscoveryClientConfiguration discoveryClientConfiguration = new CseDiscoveryClientConfiguration();
    Assert.assertTrue(discoveryClientConfiguration.cseDiscoveryClient() instanceof CseDiscoveryClient);
  }
}
