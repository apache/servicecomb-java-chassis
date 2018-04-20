package org.apache.servicecomb.loadbalance.event;

import java.util.HashMap;

import org.apache.servicecomb.foundation.common.event.AlarmEvent;

public class IsolationServerEvent extends AlarmEvent {

  private static int id = 1003;

  /**
   * msg部分字段说明：
   * currentTotalRequest:当前实例总请求数
   * currentCountinuousFailureCount:当前实例连续出错次数
   * currentErrorPercentage:当前实例出错百分比
   */
  public IsolationServerEvent(String microserviceName, long totalRequest, int currentCountinuousFailureCount,
      double currentErrorPercentage, int continuousFailureThreshold,
      int errorThresholdPercentage, long enableRequestThreshold, long singleTestTime, Type type) {
    super(type, id);
    HashMap<String, Object> msg = new HashMap<>();
    msg.put("microserviceName", microserviceName);
    msg.put("currentTotalRequest", totalRequest);
    msg.put("currentCountinuousFailureCount", currentCountinuousFailureCount);
    msg.put("currentErrorPercentage", currentErrorPercentage);
    msg.put("continuousFailureThreshold", continuousFailureThreshold);
    msg.put("errorThresholdPercentage", errorThresholdPercentage);
    msg.put("enableRequestThreshold", enableRequestThreshold);
    msg.put("singleTestTime", singleTestTime);
    super.setMsg(msg);
  }
}
