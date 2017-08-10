package io.servicecomb.core;

import io.servicecomb.core.BootListener.BootEvent;
import io.servicecomb.core.BootListener.EventType;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by l00168639 on 2017/8/10.
 */
public class TestSystemBootListener {

  @BeforeClass
  public static void setUp() {
    Deencapsulation.setField(SystemBootListener.class, "ready", false);
  }

  @Test
  public void testSystemBootListener() {
    SystemBootListener boot = new SystemBootListener();
    BootEvent event = new BootEvent();
    event.setEventType(EventType.AFTER_HANDLER);
    boot.onBootEvent(event);
    Assert.assertEquals(SystemBootListener.isReady(), false);

    event = new BootEvent();
    event.setEventType(EventType.AFTER_REGISTRY);
    boot.onBootEvent(event);
    Assert.assertEquals(SystemBootListener.isReady(), true);
  }
}
