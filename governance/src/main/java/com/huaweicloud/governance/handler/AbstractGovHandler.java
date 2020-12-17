package com.huaweicloud.governance.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.governance.event.ConfigurationChangedEvent;
import com.huaweicloud.governance.event.EventManager;

public abstract class AbstractGovHandler<PROCESSOR> implements GovHandler {
  private Map<String, PROCESSOR> map = new ConcurrentHashMap<>();

  protected AbstractGovHandler() {
    EventManager.register(this);
  }

  protected <R> PROCESSOR getActuator(String key, R policy, Function<R, PROCESSOR> func) {
    PROCESSOR processor = map.get(key);
    if (processor == null) {
      processor = func.apply(policy);
      map.put(key, processor);
    }
    return processor;
  }

  @Subscribe
  public void onDynamicConfigurationListener(ConfigurationChangedEvent event) {
    map.clear();
  }
}
