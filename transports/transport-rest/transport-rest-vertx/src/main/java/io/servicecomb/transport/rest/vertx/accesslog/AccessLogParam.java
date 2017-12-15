package io.servicecomb.transport.rest.vertx.accesslog;

import io.vertx.ext.web.RoutingContext;

public class AccessLogParam {
  private RoutingContext routingContext;

  private long startMillisecond;

  private long endMillisecond;

  public RoutingContext getRoutingContext() {
    return routingContext;
  }

  public AccessLogParam setRoutingContext(RoutingContext routingContext) {
    this.routingContext = routingContext;
    return this;
  }

  public long getStartMillisecond() {
    return startMillisecond;
  }

  public AccessLogParam setStartMillisecond(long startMillisecond) {
    this.startMillisecond = startMillisecond;
    return this;
  }

  public long getEndMillisecond() {
    return endMillisecond;
  }

  public AccessLogParam setEndMillisecond(long endMillisecond) {
    this.endMillisecond = endMillisecond;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("AccessLogParam{");
    sb.append("routingContext=").append(routingContext);
    sb.append(", startMillisecond=").append(startMillisecond);
    sb.append(", endMillisecond=").append(endMillisecond);
    sb.append('}');
    return sb.toString();
  }
}
