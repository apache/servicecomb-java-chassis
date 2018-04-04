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

package org.apache.servicecomb.samples.mwf;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.foundation.metrics.MetricsBootstrapConfig;
import org.apache.servicecomb.foundation.metrics.MetricsInitializer;
import org.apache.servicecomb.foundation.metrics.PolledEvent;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.spectator.api.CompositeRegistry;
import com.netflix.spectator.api.Measurement;
import com.netflix.spectator.api.Meter;
import com.netflix.spectator.api.Tag;

/**
 * a sample for cloud eye
 * 
 * related product only used logback or log4j2
 * both logback and log4j2 support write to different log file depend on different MDC value
 * 
 * reference sample-logback.xml and sample-log4j2.xml
 */
public class CloudEyeFilePublisher implements MetricsInitializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(CloudEyeFilePublisher.class);

  private static final Logger CLOUD_EYE_LOGGER = LoggerFactory.getLogger("cloudEyeLogger");

  private String filePrefix;

  private String hostName;

  @Override
  public void init(CompositeRegistry globalRegistry, EventBus eventBus, MetricsBootstrapConfig config) {
    eventBus.register(this);

    Microservice microservice = RegistryUtils.getMicroservice();
    filePrefix = microservice.getAppId() + "." + microservice.getServiceName();

    hostName = NetUtils.getHostName();
    if (StringUtils.isEmpty(hostName)) {
      hostName = NetUtils.getHostAddress();
    }

    System.setProperty("cloudEye.logDir",
        DynamicPropertyFactory
            .getInstance()
            .getStringProperty("cloudEye.logDir", "logs")
            .get());
  }

  @Subscribe
  public void onPolledEvent(PolledEvent event) {
    long now = System.currentTimeMillis();
    for (Meter meter : event.getMeters()) {
      for (Measurement measurement : meter.measure()) {
        logMeasurement(measurement, now);
      }
    }
  }

  protected void logMeasurement(Measurement measurement, long now) {
    String metricKey = generateMetricKey(measurement);

    MDC.put("fileName", filePrefix + "." + metricKey + ".dat");

    CloudEyeMetricModel metricModel = new CloudEyeMetricModel();
    metricModel.setNode(hostName);
    metricModel.setTimestamp(now);
    metricModel.getDynamicValue().put(metricKey, String.valueOf(measurement.value()));

    CloudEyeModel model = new CloudEyeModel();
    model.setPlugin_id(filePrefix);
    model.setMetric(metricModel);

    try {
      CLOUD_EYE_LOGGER.info(JsonUtils.writeValueAsString(model));
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to write cloud eye log.", e);
    }
  }

  protected String generateMetricKey(Measurement measurement) {
    StringBuilder sb = new StringBuilder();
    sb.append(measurement.id().name());
    for (Tag tag : measurement.id().tags()) {
      sb.append('.').append(tag.value());
    }
    return sb.toString();
  }
}
