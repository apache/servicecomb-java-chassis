package org.apache.servicecomb.foundation.vertx;

import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.core.dns.AddressResolverOptions;

public class TestAddressResoverConfig {

  @BeforeClass
  public static void classSetup() {
    ArchaiusUtils.resetConfig();
  }

  @AfterClass
  public static void classTeardown() {
    ArchaiusUtils.resetConfig();
  }


  @Test
  public void testGetResover() {
    ArchaiusUtils.resetConfig();
    ArchaiusUtils.setProperty("addressResolver.servers", "8.8.8.8,8.8.4.4");
    ArchaiusUtils.setProperty("addressResolver.optResourceEnabled", true);
    ArchaiusUtils.setProperty("addressResolver.cacheMinTimeToLive", 0);
    ArchaiusUtils.setProperty("addressResolver.cacheMaxTimeToLive", 10000);
    ArchaiusUtils.setProperty("addressResolver.cacheNegativeTimeToLive", 0);
    ArchaiusUtils.setProperty("addressResolver.queryTimeout", 1000);
    ArchaiusUtils.setProperty("addressResolver.maxQueries", 3);
    ArchaiusUtils.setProperty("addressResolver.test.maxQueries", 3);
    ArchaiusUtils.setProperty("addressResolver.rdFlag", true);
    ArchaiusUtils.setProperty("addressResolver.searchDomains",
        "default.svc.local.cluster,svc.local.cluster,local.cluster");
    ArchaiusUtils.setProperty("addressResolver.test.searchDomains",
        "test.svc.local.cluster,svc.local.cluster,local.cluster");
    ArchaiusUtils.setProperty("addressResolver.ndots", 3);
    ArchaiusUtils.setProperty("addressResolver.rotateServers", true);
    AddressResolverOptions aroc = AddressResolverConfig.getAddressResover("test");
    Assert.assertEquals(2, aroc.getServers().size());
    Assert.assertEquals("8.8.8.8", aroc.getServers().get(0));
    Assert.assertEquals("test.svc.local.cluster", aroc.getSearchDomains().get(0));
    AddressResolverOptions aroc1 = AddressResolverConfig.getAddressResover("test1");
    Assert.assertEquals("default.svc.local.cluster", aroc1.getSearchDomains().get(0));
  }

  @Test
  public void testGetResoverDefault() {
    ArchaiusUtils.resetConfig();
    ArchaiusUtils.setProperty("addressResolver.servers", "8.8.8.8,8.8.4.4");
    ArchaiusUtils.setProperty("addressResolver.maxQueries", 3);
    ArchaiusUtils.setProperty("addressResolver.rdFlag", false);
    AddressResolverOptions aroc = AddressResolverConfig.getAddressResover("test");
    Assert.assertEquals(2, aroc.getServers().size());
    Assert.assertEquals(3, aroc.getMaxQueries());
    Assert.assertEquals(Integer.MAX_VALUE, aroc.getCacheMaxTimeToLive());
    Assert.assertTrue(aroc.isOptResourceEnabled());
    Assert.assertNull(aroc.getSearchDomains());
  }
}
