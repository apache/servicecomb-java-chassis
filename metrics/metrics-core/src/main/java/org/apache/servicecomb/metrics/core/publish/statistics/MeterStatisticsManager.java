package org.apache.servicecomb.metrics.core.publish.statistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerfGroup;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;

public class MeterStatisticsManager {

  public static Map<String, MeterDetailStatisticsModel> statisticsOperationMap = new HashMap<>();


  public static void loadMeterDetailStatisticsModelFromPerfGroup(OperationPerfGroup perfGroup,
      MeterStatisticsMeterType type) {
    List<OperationPerf> operationPerfs = perfGroup.getOperationPerfs();

    String status = perfGroup.getTransport() + "." + perfGroup.getStatus();

    for (OperationPerf operationPerf : operationPerfs) {
      String operation = operationPerf.getOperation();
      MeterDetailStatisticsModel detailStatisticsModel = statisticsOperationMap
          .computeIfAbsent(operation, key -> new MeterDetailStatisticsModel(type, key));
      detailStatisticsModel.getStatus().add(status);
      detailStatisticsModel.initOrAddPerfInfo(operationPerf, status);
    }
  }


  /**
   * 从 perfInfo 中获取 保留三位小数的字符串
   * @param perfInfo
   * @return
   */
  public static String getDetailsFromPerf(PerfInfo perfInfo) {
    String result = "";
    if (perfInfo != null) {
      result = String.format("%.3f/%.3f", perfInfo.calcMsLatency(), perfInfo.getMsMaxLatency());
    }
    return result;
  }
}
