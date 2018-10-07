package org.apache.servicecomb.metrics.core.publish.model;

import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroups;

public class EdgePublishModel extends ConsumerPublishModel {
  private OperationPerfGroups operationPerfGroups;

  public OperationPerfGroups getOperationPerfGroups() {
    return operationPerfGroups;
  }

  public void setOperationPerfGroups(OperationPerfGroups operationPerfGroups) {
    this.operationPerfGroups = operationPerfGroups;
  }
}
