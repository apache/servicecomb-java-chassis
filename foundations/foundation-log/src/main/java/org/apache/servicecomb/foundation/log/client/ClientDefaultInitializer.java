package org.apache.servicecomb.foundation.log.client;

import org.apache.servicecomb.core.event.InvocationFinishEvent;
import org.apache.servicecomb.foundation.log.LogConfig;
import org.apache.servicecomb.foundation.log.LogInitializer;
import org.apache.servicecomb.foundation.log.core.LogGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ClientDefaultInitializer implements LogInitializer {
  private static Logger LOGGER = LoggerFactory.getLogger("outlog");

  private LogGenerator logGenerator;

  @Override
  public void init(EventBus eventBus, LogConfig logConfig) {
    if (!logConfig.isClientLogEnabled()) {
      return;
    }
    logGenerator = new LogGenerator(logConfig.getClientLogPattern());
    eventBus.register(this);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onRequestOut(InvocationFinishEvent finishEvent) {
    LOGGER.info(logGenerator.generateClientLog(finishEvent));
  }
}
