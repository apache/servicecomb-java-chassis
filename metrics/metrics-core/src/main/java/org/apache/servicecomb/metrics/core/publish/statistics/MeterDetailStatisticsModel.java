package org.apache.servicecomb.metrics.core.publish.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.metrics.core.meter.invocation.MeterInvocationConst;
import org.apache.servicecomb.metrics.core.publish.model.invocation.OperationPerf;
import org.apache.servicecomb.metrics.core.publish.model.invocation.PerfInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeterDetailStatisticsModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(MeterDetailStatisticsModel.class);

  private static final String PRODUCER_DETAILS_FORMAT = "        %-7s: %-22s %-12s: %-22s %-11s: %-22s %-11s: %s\n"
      + "        %-7s: %-22s %-12s: %-22s %-11s: %-22s %-11s: %s\n";

  private static final String CONSUMER_DETAILS_FORMAT = "        %-17s: %-22s %-12s: %-22s %-16s: %-22s %-12s: %s\n"
      + "        %-17s: %-22s %-12s: %-22s %-16s: %-22s %-12s: %s\n"
      + "        %-17s: %-22s %-12s: %s\n";

  private static final String EDGE_DETAILS_FORMAT = "        %-17s: %-22s %-12s: %-22s %-17s: %-22s %-12s: %s\n"
      + "        %-17s: %-22s %-12s: %-22s %-17s: %-22s %-12s: %s\n"
      + "        %-17s: %-22s %-12s: %-22s %-17s: %-22s %-12s: %s\n"
      + "        %-17s: %-22s %-12s: %s\n";

  private MeterStatisticsMeterType type;

  private String operation;

  // status : rest.400
  private List<String> status = new ArrayList<>();

  //key : status .  tag -> value
  private Map<String, Map<String, String>> detailsStatistics = new HashMap<>();

  //keep the order
  private static Map<String, String> producerKeys = new LinkedHashMap<>();

  private static Map<String, String> consumersKeys = new LinkedHashMap<>();

  private static Map<String, String> edgeKeys = new LinkedHashMap<>();

  static {
    //producers
    producerKeys.put("prepare", MeterInvocationConst.STAGE_PREPARE);
    producerKeys.put("queue", MeterInvocationConst.STAGE_EXECUTOR_QUEUE);
    producerKeys.put("filtersReq", MeterInvocationConst.STAGE_SERVER_FILTERS_REQUEST);
    producerKeys.put("handlersReq", MeterInvocationConst.STAGE_HANDLERS_REQUEST);
    producerKeys.put("execute", MeterInvocationConst.STAGE_EXECUTION);
    producerKeys.put("handlersResp", MeterInvocationConst.STAGE_HANDLERS_RESPONSE);
    producerKeys.put("filtersResp", MeterInvocationConst.STAGE_SERVER_FILTERS_RESPONSE);
    producerKeys.put("sendResp", MeterInvocationConst.STAGE_PRODUCER_SEND_RESPONSE);
    //consumers
    consumersKeys.put("prepare", MeterInvocationConst.STAGE_PREPARE);
    consumersKeys.put("handlersReq", MeterInvocationConst.STAGE_HANDLERS_REQUEST);
    consumersKeys.put("clientFiltersReq", MeterInvocationConst.STAGE_CLIENT_FILTERS_REQUEST);
    consumersKeys.put("sendReq", MeterInvocationConst.STAGE_CONSUMER_SEND_REQUEST);
    consumersKeys.put("getConnect", MeterInvocationConst.STAGE_CONSUMER_GET_CONNECTION);
    consumersKeys.put("writeBuf", MeterInvocationConst.STAGE_CONSUMER_WRITE_TO_BUF);
    consumersKeys.put("waitResp", MeterInvocationConst.STAGE_CONSUMER_WAIT_RESPONSE);
    consumersKeys.put("wakeConsumer", MeterInvocationConst.STAGE_CONSUMER_WAKE_CONSUMER);
    consumersKeys.put("clientFiltersResp", MeterInvocationConst.STAGE_CLIENT_FILTERS_RESPONSE);
    consumersKeys.put("handlersResp", MeterInvocationConst.STAGE_HANDLERS_RESPONSE);
    //edge
    edgeKeys.put("prepare", MeterInvocationConst.STAGE_PREPARE);
    edgeKeys.put("queue", MeterInvocationConst.STAGE_EXECUTOR_QUEUE);
    edgeKeys.put("serverFiltersReq", MeterInvocationConst.STAGE_SERVER_FILTERS_REQUEST);
    edgeKeys.put("handlersReq", MeterInvocationConst.STAGE_HANDLERS_REQUEST);
    edgeKeys.put("clientFiltersReq", MeterInvocationConst.STAGE_CLIENT_FILTERS_REQUEST);
    edgeKeys.put("sendReq", MeterInvocationConst.STAGE_CONSUMER_SEND_REQUEST);
    edgeKeys.put("getConnect", MeterInvocationConst.STAGE_CONSUMER_GET_CONNECTION);
    edgeKeys.put("writeBuf", MeterInvocationConst.STAGE_CONSUMER_WRITE_TO_BUF);
    edgeKeys.put("waitResp", MeterInvocationConst.STAGE_CONSUMER_WAIT_RESPONSE);
    edgeKeys.put("wakeConsumer", MeterInvocationConst.STAGE_CONSUMER_WAKE_CONSUMER);
    edgeKeys.put("clientFiltersResp", MeterInvocationConst.STAGE_CLIENT_FILTERS_RESPONSE);
    edgeKeys.put("handlersResp", MeterInvocationConst.STAGE_HANDLERS_RESPONSE);
    edgeKeys.put("serverFiltersResp", MeterInvocationConst.STAGE_SERVER_FILTERS_RESPONSE);
    edgeKeys.put("sendResp", MeterInvocationConst.STAGE_PRODUCER_SEND_RESPONSE);
  }

  public MeterDetailStatisticsModel(MeterStatisticsMeterType type, String operation) {
    this.type = type;
    this.operation = operation;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public List<String> getStatus() {
    return status;
  }

  public void setStatus(List<String> status) {
    this.status = status;
  }

  public Map<String, Map<String, String>> getDetailsStatistics() {
    return detailsStatistics;
  }

  public void setDetailsStatistics(
      Map<String, Map<String, String>> detailsStatistics) {
    this.detailsStatistics = detailsStatistics;
  }

  public void initOrAddPerfInfo(OperationPerf operationPerf, String status) {

    Map<String, String> meterDetails = new LinkedHashMap<>();

    switch (this.type) {
      case PRODUCER:
        //producer
        producerKeys.forEach((key, value) -> {
          PerfInfo stage = operationPerf.findStage(value);
          meterDetails.put(key, MeterStatisticsManager.getDetailsFromPerf(stage));
        });
        break;
      case CONSUMER:
        //consumer
        consumersKeys.forEach((key, value) -> {
          PerfInfo stage = operationPerf.findStage(value);
          meterDetails.put(key, MeterStatisticsManager.getDetailsFromPerf(stage));
        });
        break;
      case EDGE:
        //edge
        edgeKeys.forEach((key, value) -> {
          PerfInfo stage = operationPerf.findStage(value);
          meterDetails.put(key, MeterStatisticsManager.getDetailsFromPerf(stage));
        });
        break;
      default:
        break;
    }

    if (this.detailsStatistics.containsKey(status)) {
      LOGGER.warn("the status key is duplicate {}/{}/{}", this.type.name(), this.operation, this.status);
    }

    this.detailsStatistics.put(status, meterDetails);
  }

  public StringBuilder getFormatDetails() {
    StringBuilder stringBuilder = new StringBuilder();
    //operation
    stringBuilder.append("    ")
        .append(this.operation)
        .append(":\n");
    // status
    switch (this.type) {
      case PRODUCER:
        for (String key : detailsStatistics.keySet()) {
          stringBuilder.append("      ")
              .append(key)
              .append(":\n");
          Object[] params = initDetailsArray(detailsStatistics.get(key));
          stringBuilder.append(String.format(PRODUCER_DETAILS_FORMAT, params));
        }
        break;
      case CONSUMER:
        for (String key : detailsStatistics.keySet()) {
          stringBuilder.append("      ")
              .append(key)
              .append(":\n");
          Object[] params = initDetailsArray(detailsStatistics.get(key));
          stringBuilder.append(String.format(CONSUMER_DETAILS_FORMAT, params));
        }
        break;
      case EDGE:
        for (String key : detailsStatistics.keySet()) {
          stringBuilder.append("      ")
              .append(key)
              .append(":\n");
          Object[] params = initDetailsArray(detailsStatistics.get(key));
          stringBuilder.append(String.format(EDGE_DETAILS_FORMAT, params));
        }
        break;
      default:
        break;
    }

    return stringBuilder;
  }

  public String[] initDetailsArray(Map<String, String> detailsMap) {
    String[] array = new String[detailsMap.keySet().size() * 2];
    int index = 0;
    for (String key : detailsMap.keySet()) {
      array[index++] = key;
      array[index++] = detailsMap.get(key);
    }
    return array;
  }
}
