package org.apache.servicecomb.springboot.starter.discovery;

import org.junit.Assert;
import org.junit.Test;

import com.netflix.client.config.IClientConfig;

import mockit.Injectable;

public class TestCseRibbonClientConfiguration {
  @Test
  public void testCseRibbonClientConfiguration(@Injectable IClientConfig clientConfig) {
    CseRibbonClientConfiguration config = new CseRibbonClientConfiguration();
    Assert.assertTrue(config.ribbonServerList(clientConfig) instanceof ServiceCombServerList);
  }
}
