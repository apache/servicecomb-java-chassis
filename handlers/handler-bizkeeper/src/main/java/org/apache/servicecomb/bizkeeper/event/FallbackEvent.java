package org.apache.servicecomb.bizkeeper.event;

import java.util.HashMap;

import org.apache.servicecomb.bizkeeper.FallbackPolicy;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.event.AlarmEvent;

public class FallbackEvent extends AlarmEvent {

  private static int id = 1002;

  /**
   * msg部分字段说明：
   * invocationQualifiedName:当前调用的接口
   * policy:当前容错策略
   */
  public FallbackEvent(FallbackPolicy policy, Invocation invocation, Type type) {
    super(type, id);
    HashMap<String, Object> msg = new HashMap<>();
    msg.put("microserviceName", invocation.getMicroserviceName());
    msg.put("invocationQualifiedName", invocation.getInvocationQualifiedName());
    msg.put("policy", policy.name());
    super.setMsg(msg);
  }

}
