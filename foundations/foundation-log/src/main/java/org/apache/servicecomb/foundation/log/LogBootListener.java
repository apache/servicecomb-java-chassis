package org.apache.servicecomb.foundation.log;

import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.foundation.common.event.EventManager;

public class LogBootListener implements BootListener {

  private LogBootstrap logBootstrap = new LogBootstrap();

  @Override
  public void onAfterRegistry(BootEvent event) {
    logBootstrap.start(EventManager.getEventBus());
  }

  @Override
  public void onBeforeClose(BootEvent event) {
    logBootstrap.shutdown();
  }
}
