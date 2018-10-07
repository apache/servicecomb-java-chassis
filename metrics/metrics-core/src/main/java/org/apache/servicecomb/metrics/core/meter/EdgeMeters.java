package org.apache.servicecomb.metrics.core.meter;

import org.apache.servicecomb.metrics.core.meter.invocation.AbstractInvocationMeters;
import org.apache.servicecomb.metrics.core.meter.invocation.EdgeInvocationMeters;

import com.netflix.spectator.api.Registry;

public class EdgeMeters {
  private AbstractInvocationMeters invocationMeters;


  public EdgeMeters(Registry registry) {
    this.invocationMeters = new EdgeInvocationMeters(registry);
  }

  public AbstractInvocationMeters getInvocationMeters() {
    return invocationMeters;
  }
}
