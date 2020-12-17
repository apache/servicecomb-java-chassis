package com.huaweicloud.governance.handler;

import java.time.Duration;

import com.huaweicloud.governance.policy.BulkheadPolicy;
import com.huaweicloud.governance.policy.Policy;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;

public class BulkheadHandler extends AbstractGovHandler<Bulkhead> {

  @Override
  public <RESULT> DecorateCheckedSupplier<RESULT> process(DecorateCheckedSupplier<RESULT> supplier, Policy policy) {
    Bulkhead bulkhead = getActuator("servicecomb.bulkhead." + policy.name(), (BulkheadPolicy) policy,
        this::getBulkhead);
    return supplier.withBulkhead(bulkhead);
  }

  @Override
  public HandlerType type() {
    return HandlerType.SERVER;
  }

  private Bulkhead getBulkhead(BulkheadPolicy policy) {
    BulkheadConfig config = BulkheadConfig.custom()
        .maxConcurrentCalls(policy.getMaxConcurrentCalls())
        .maxWaitDuration(Duration.ofMillis(policy.getMaxWaitDuration()))
        .build();

    BulkheadRegistry registry = BulkheadRegistry.of(config);

    return registry.bulkhead(policy.name());
  }
}
