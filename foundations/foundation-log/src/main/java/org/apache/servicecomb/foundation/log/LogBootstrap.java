package org.apache.servicecomb.foundation.log;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;

import com.google.common.eventbus.EventBus;

public class LogBootstrap {
  private static LogConfig config = LogConfig.INSTANCE;

  private EventBus eventBus;

  public void start(EventBus eventBus) {
    this.eventBus = eventBus;
    SPIServiceUtils.getSortedService(LogInitializer.class)
        .forEach(initializer -> initializer.init(eventBus, config));
  }

  public void shutdown() {
    SPIServiceUtils.getSortedService(LogInitializer.class)
        .forEach(LogInitializer::destroy);
  }
}
