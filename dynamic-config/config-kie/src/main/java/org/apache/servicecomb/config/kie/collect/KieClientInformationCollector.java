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

package org.apache.servicecomb.config.kie.collect;

import org.apache.servicecomb.core.bootup.BootUpInformationCollector;
import org.apache.servicecomb.deployment.Deployment;
import org.apache.servicecomb.deployment.SystemBootstrapInfo;

import java.util.Objects;


public class KieClientInformationCollector implements BootUpInformationCollector {
  @Override
  public String collect() {
    return "Kie Center: " + getCenterInfo(Deployment.getSystemBootStrapInfo("KieCenter"));
  }

  @Override
  public int getOrder() {
    return 2;
  }

  private String getCenterInfo(SystemBootstrapInfo systemBootstrapInfo) {
    return Objects.isNull(systemBootstrapInfo) ? "not exist" : systemBootstrapInfo.getAccessURL().toString();
  }
}
