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
package org.apache.servicecomb.demo.springmvc.server;

import org.apache.servicecomb.core.bootup.ConfigurationProblemsAlarmEvent;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

@Component
public class ConfigurationProblemsCollectorTest implements CategorizedTestCase {
  private ConfigurationProblemsAlarmEvent event;

  public ConfigurationProblemsCollectorTest() {
    EventManager.register(this);
  }

  @Subscribe
  public void onConfigurationProblemsAlarmEvent(ConfigurationProblemsAlarmEvent event) {
    this.event = event;
  }

  @Override
  public void testRestTransport() throws Exception {
    TestMgr.check(event != null, true);
    TestMgr.check(event.getProblems(), "Configurations warnings:\n"
        + "Configurations with prefix `cse` is deprecated, use `servicecomb` instead. Find keys [cse.test.duplicate1]\n"
        + "Configurations `APPLICATION_ID` is deprecated, use `servicecomb.service.application` instead.\n"
        + "Configurations with prefix `service_description` is deprecated, "
        + "use `servicecomb.service` instead. Find keys [service_description.name, service_description.paths, "
        + "service_description.version]\n");
  }
}
