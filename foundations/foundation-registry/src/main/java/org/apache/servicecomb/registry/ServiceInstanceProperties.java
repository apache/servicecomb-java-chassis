package org.apache.servicecomb.registry;

public class ServiceInstanceProperties {
  private String initialStatus = "UP";

  public String getInitialStatus() {
    return initialStatus;
  }

  public void setInitialStatus(String initialStatus) {
    this.initialStatus = initialStatus;
  }
}
