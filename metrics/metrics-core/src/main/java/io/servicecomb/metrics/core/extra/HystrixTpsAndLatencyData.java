package io.servicecomb.metrics.core.extra;

public class HystrixTpsAndLatencyData {
  private final String operationName;

  private final long successCount;

  private final long failureCount;

  private final int operationLatency;

  private final long windowInMilliseconds;

  public String getOperationName() {
    return operationName;
  }

  public long getSuccessCount() {
    return successCount;
  }

  public long getFailureCount() {
    return failureCount;
  }

  public int getOperationLatency() {
    return operationLatency;
  }

  public long getWindowInMilliseconds() {
    return windowInMilliseconds;
  }

  public HystrixTpsAndLatencyData(String operationName, long successCount, long failureCount, int operationLatency,
      long windowInMilliseconds) {
    this.operationName = operationName;
    this.successCount = successCount;
    this.failureCount = failureCount;
    this.operationLatency = operationLatency;
    this.windowInMilliseconds = windowInMilliseconds;
  }
}
