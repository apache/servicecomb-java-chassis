package org.apache.servicecomb.registry.nacos;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.listener.Subscriber;

public class InstancesChangeEventListener extends Subscriber<InstancesChangeEvent> {
  private final NacosDiscovery nacosDiscovery;

  @Autowired
  public InstancesChangeEventListener(NacosDiscovery nacosDiscovery) {
    this.nacosDiscovery = nacosDiscovery;
  }

  @Override
  public void onEvent(InstancesChangeEvent event) {
    nacosDiscovery.onInstanceChangedEvent(event);
  }

  @Override
  public Class<? extends Event> subscribeType() {
    return InstancesChangeEvent.class;
  }
}
