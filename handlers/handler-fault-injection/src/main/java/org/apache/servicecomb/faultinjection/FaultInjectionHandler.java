/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.faultinjection;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.context.HttpStatus;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import static org.apache.servicecomb.faultinjection.FaultInjectionConst.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fault injection handler which injects the delay/abort for requests based on
 * the percentage configured in service file.
 *
 */
public class FaultInjectionHandler implements Handler {
  private static final Logger LOGGER = LoggerFactory.getLogger(FaultInjectionHandler.class);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

    //if no transport then call next handler.
    if ((invocation.getTransport() != null) && (invocation.getTransport().getName() != null)) {

      // prepare the key and lookup for request count.
      String key = invocation.getTransport().getName() + invocation.getMicroserviceQualifiedName();

      AtomicLong reqCount = FaultInjectionUtil.getOperMetTotalReq(key);
      long reqCountCurrent = reqCount.get();
      // increment the request count here after checking the delay/abort condition.
      reqCount.incrementAndGet();

      // get the config values related to delay percentage.
      int delayPercent = getFaultInjectionConfig(invocation,
          "delay.percent",
          FAULT_INJECTION_DELAY_PERCENTAGE_DEFAULT);

      // check fault delay condition.
      boolean isDelay = checkFaultInjectionDelayAndAbort(reqCountCurrent, delayPercent);
      if (isDelay) {
        LOGGER.info("delay is added for the request by fault inject handler");
        long delay = getFaultInjectionConfig(invocation,
            "delay.fixedDelay",
            FAULT_INJECTION_DELAY_DEFAULT);

        Thread.sleep(delay);
      }

      // get the config values related to delay.
      int abortPercent = getFaultInjectionConfig(invocation,
          "abort.percent",
          FAULT_INJECTION_ABORT_PERCENTAGE_DEFAULT);

      // check fault delay condition.
      boolean isAbort = checkFaultInjectionDelayAndAbort(reqCountCurrent, abortPercent);
      if (isAbort) {
        // get the config values related to delay percentage.
        int errorCode = getFaultInjectionConfig(invocation,
            "abort.httpStatus",
            FAULT_INJECTION_ABORT_ERROR_MSG_DEFAULT);
        // if request need to be abort then return failure with given error code
        CommonExceptionData errorData = new CommonExceptionData("aborted by fault inject");
        asyncResp.consumerFail(
            new InvocationException(new HttpStatus(errorCode, "aborted by fault inject"), errorData));
        return;
      }
    }

    // if no delay and no abort then continue to next handler.
    invocation.next(asyncResp);
  }

  /**
   * It will check the delay/abort condition based on request count and percentage
   * received.
   * 
   * @param reqCount
   * @param percentage
   * @param key
   * @return true/false
   */
  private boolean checkFaultInjectionDelayAndAbort(long reqCount, int percentage) {
    /*
     * Example: delay/abort percentage configured is 10% and Get the count(suppose
     * if it is 10th request) from map and calculate resultNew(10th request) and
     * requestOld(9th request). Like this for every request it will calculate
     * current request count and previous count. if both not matched need to add
     * delay/abort otherwise no need to add.
     */

    // calculate the value with current request count.
    long resultNew = (reqCount * percentage) / 100;

    // calculate the value with previous count value.
    long resultOld = ((reqCount - 1) * percentage) / 100;

    // if both are not matching then delay/abort should be added.
    if (resultNew != resultOld) {
      return true;
    }
    return false;
  }

  /**
   * Handles the reading fault injection configuration.
   * 
   * @param invocation
   *            invocation of request
   * @param key
   *            configuration key
   * @return configuration value
   */
  private int getFaultInjectionConfig(Invocation invocation, String key, int defaultValue) {
    int value = 0;
    String config;
    FaultInjectionConfig faultCfg = new FaultInjectionConfig();

    //first need to check in config center map which has high priority.
    Map<String, AtomicInteger> cfgMap = FaultInjectionUtil.getConfigCenterMap();

    // get the config base on priority. operationName-->schema-->service-->global
    String operationName = invocation.getOperationName();
    String schema = invocation.getSchemaId();
    String serviceName = invocation.getMicroserviceName();

    if (operationName != null && schema != null && serviceName != null) {
      if (invocation.getTransport().getName().equals(FAULTINJECTION_HIGHWAY_TRANSPORT)) {
        config = CONSUMER_FAULTINJECTION + serviceName + ".schemas." + schema + ".operations." + operationName + "."
            + CONSUMER_FAULTINJECTION_HIGHWAY + key;
      } else {
        config = CONSUMER_FAULTINJECTION + serviceName + ".schemas." + schema + ".operations." + operationName + "."
            + CONSUMER_FAULTINJECTION_REST + key;
      }

      if (cfgMap.containsKey(config)) {
        return cfgMap.get(config).get();
      }

      value = faultCfg.getConfigVal(config, FAULT_INJECTION_CFG_NULL);
      if ((value != FAULT_INJECTION_CFG_NULL)) {
        return value;
      }
    }

    if (schema != null && serviceName != null) {
      if (invocation.getTransport().getName().equals(FAULTINJECTION_HIGHWAY_TRANSPORT)) {
        config = CONSUMER_FAULTINJECTION + serviceName + ".schemas." + schema + "."
            + CONSUMER_FAULTINJECTION_HIGHWAY + key;
      } else {
        config = CONSUMER_FAULTINJECTION + serviceName + ".schemas." + schema + "."
            + CONSUMER_FAULTINJECTION_REST + key;
      }

      if (cfgMap.containsKey(config)) {
        return cfgMap.get(config).get();
      }

      value = faultCfg.getConfigVal(config, FAULT_INJECTION_CFG_NULL);
      if ((value != FAULT_INJECTION_CFG_NULL)) {
        return value;
      }
    }

    if (serviceName != null) {
      if (invocation.getTransport().getName().equals(FAULTINJECTION_HIGHWAY_TRANSPORT)) {
        config = CONSUMER_FAULTINJECTION + serviceName + "."
            + CONSUMER_FAULTINJECTION_HIGHWAY + key;
      } else {
        config = CONSUMER_FAULTINJECTION + serviceName + "."
            + CONSUMER_FAULTINJECTION_REST + key;
      }

      if (cfgMap.containsKey(config)) {
        return cfgMap.get(config).get();
      }

      value = faultCfg.getConfigVal(config, FAULT_INJECTION_CFG_NULL);
      if ((value != FAULT_INJECTION_CFG_NULL)) {
        return value;
      }
    }

    if (invocation.getTransport().getName().equals(FAULTINJECTION_HIGHWAY_TRANSPORT)) {
      config = CONSUMER_FAULTINJECTION_GLOBAL
          + CONSUMER_FAULTINJECTION_HIGHWAY + key;
    } else {
      config = CONSUMER_FAULTINJECTION_GLOBAL
          + CONSUMER_FAULTINJECTION_REST + key;
    }

    if (cfgMap.containsKey(config)) {
      return cfgMap.get(config).get();
    }

    value = faultCfg.getConfigVal(config, defaultValue);
    return value;
  }
}
