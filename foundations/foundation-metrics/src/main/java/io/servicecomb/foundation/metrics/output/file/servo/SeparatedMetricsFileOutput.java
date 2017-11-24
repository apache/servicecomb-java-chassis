/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.foundation.metrics.output.file.servo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;

import io.servicecomb.foundation.metrics.MetricsServoRegistry;
import io.servicecomb.foundation.metrics.output.file.MetricsFileOutput;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;

@Component
public class SeparatedMetricsFileOutput extends MetricsFileOutput {

  private final String name;
  private MetricsServoRegistry registry;



  @Autowired
  public SeparatedMetricsFileOutput(MetricsServoRegistry registry) {
    this.registry = registry;
    String hostName;
    try {
      InetAddress localHost = InetAddress.getLocalHost();
      hostName = localHost.getHostName();
    } catch (UnknownHostException e) {
      e.printStackTrace();
      hostName = "UnknownHost";
    }

    if (RegistryUtils.getServiceRegistry() != null) {
      Microservice microservice = RegistryUtils.getMicroservice();
      name = String.join(".", microservice.getAppId(), microservice.getServiceName());
    } else {
      name = String.join(".", hostName, "test");
    }

    this.init();
  }

  @Override
  public void init() {
    PollScheduler scheduler = PollScheduler.getInstance();
    if (!scheduler.isStarted()) {
      scheduler.start();
    }

    if (isEnabled()) {
      MetricObserver fileObserver = new SeparatedMetricObserver(name, getFilePath(), getFileSize(), registry);
      MetricObserver fileTransform = new CounterToRateMetricTransform(fileObserver, getMetricPoll(), TimeUnit.SECONDS);
      PollRunnable fileTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL,
          fileTransform);
      scheduler.addPoller(fileTask, getMetricPoll(), TimeUnit.SECONDS);
    }
  }
}
