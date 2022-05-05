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
package org.apache.servicecomb.it.deploy;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.servicecomb.it.ITUtils;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.consumer.MicroserviceVersionRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MicroserviceDeploy extends NormalDeploy {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceDeploy.class);

  private final MicroserviceDeployDefinition microserviceDeployDefinition;

  public MicroserviceDeploy(DeployDefinition deployDefinition) {
    super(deployDefinition);
    this.microserviceDeployDefinition = (MicroserviceDeployDefinition) deployDefinition;

    this.microserviceDeployDefinition.setStartCompleteLog("ServiceComb is ready.");
  }

  public MicroserviceDeployDefinition getMicroserviceDeployDefinition() {
    return microserviceDeployDefinition;
  }

  @Override
  protected String[] createCmds() {
    // must set jar at the end of the cmds
    return new String[] {"java", "-jar"};
  }

  @Override
  protected String[] addArgs(String[] cmds) {
    // add jar
    return ArrayUtils.addAll(super.addArgs(cmds),
        "-DselfController=" + RegistrationManager.INSTANCE.getMicroserviceInstance().getInstanceId(),
        "-Dservice_description.name=" + microserviceDeployDefinition.getMicroserviceName(),
        deployDefinition.getCmd());
  }

  public void ensureReady() throws Throwable {
    MicroserviceVersionRule microserviceVersionRule = DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceVersionRule(microserviceDeployDefinition.getAppId(),
            microserviceDeployDefinition.getMicroserviceName(),
            microserviceDeployDefinition.getVersion());
    if (microserviceVersionRule.getInstances().size() > 0) {
      LOGGER.info("{} already ready.", microserviceDeployDefinition.getDisplayName());
      return;
    }

    deploy();
    waitStartComplete();
    ITUtils.waitMicroserviceReady(microserviceDeployDefinition.getAppId(),
        microserviceDeployDefinition.getMicroserviceName(),
        microserviceDeployDefinition.getVersion(),
        1);
  }

  @Override
  public void stop() {
    if (subProcess == null) {
      return;
    }

    sendCommand("ms-stop");
    waitStop();
  }
}
