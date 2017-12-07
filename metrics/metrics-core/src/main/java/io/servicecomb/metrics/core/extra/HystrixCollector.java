package io.servicecomb.metrics.core.extra;

import java.util.Map;

public interface HystrixCollector {
  Map<String, Number> collect();
}
