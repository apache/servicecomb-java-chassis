package org.apache.servicecomb.foundation.log.server;

import org.apache.servicecomb.core.event.ServerAccessLogEvent;
import org.apache.servicecomb.foundation.log.LogConfig;
import org.apache.servicecomb.foundation.log.LogInitializer;
import org.apache.servicecomb.foundation.log.core.LogGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class ServerDefaultInitializer implements LogInitializer {

  private static Logger LOGGER = LoggerFactory.getLogger("accesslog");

  private LogGenerator logGenerator;

  @Override
  public void init(EventBus eventBus, LogConfig logConfig) {
    if (!logConfig.isServerLogEnabled()) {
      return;
    }
    logGenerator = new LogGenerator(logConfig.getServerLogPattern());
    eventBus.register(this);
  }

  @Subscribe
  @AllowConcurrentEvents
  public void onRequestReceived(ServerAccessLogEvent accessLogEvent) {
    LOGGER.info(logGenerator.generateServerLog(accessLogEvent));
  }
}
