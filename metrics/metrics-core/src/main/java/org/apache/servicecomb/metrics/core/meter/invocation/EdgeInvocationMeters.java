package org.apache.servicecomb.metrics.core.meter.invocation;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;

public class EdgeInvocationMeters extends ConsumerInvocationMeters {
  public EdgeInvocationMeters(Registry registry) {
    super(registry);
  }

  @Override
  protected AbstractInvocationMeter createMeter(Id id, Invocation invocation, Response response) {
    return new EdgeInvocationMeter(registry, id, invocation, response);
  }
}
