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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.servicecomb.serviceregistry.api.registry.ServiceCenterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import io.swagger.util.Json;

public class ServiceCenterDeploy extends NormalDeploy {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterDeploy.class);

  public ServiceCenterDeploy() {
    super(new DeployDefinition());

    deployDefinition.setDeployName("serviceCenter");
    deployDefinition.setDisplayName("serviceCenter");
    if (SystemUtils.IS_OS_WINDOWS) {
      deployDefinition.setCmd("service-center.exe");
    } else {
      deployDefinition.setCmd("service-center");
    }
    deployDefinition.setStartCompleteLog("server is ready");
  }

  public void ensureReady() throws Throwable {
    // check if is running
    // {"version":"1.0.0","buildTag":"20180608145515.1.0.0.b913a2d","runMode":"dev","apiVersion":"3.0.0"}
    try {
      String address = "http://localhost:30100/version";
      ServiceCenterInfo serviceCenterInfo = new RestTemplate().getForObject(address, ServiceCenterInfo.class);
      if (serviceCenterInfo != null && serviceCenterInfo.getVersion() != null) {
        LOGGER.info("{} already started, {}.", deployDefinition.getDisplayName(), Json.pretty(serviceCenterInfo));
        return;
      }
    } catch (Throwable e) {
      LOGGER.info("failed to get ServiceCenter version, message={}", e.getMessage());
    }

    initServiceCenterCmd();
    LOGGER.info("definition of {} is: {}", deployDefinition.getDeployName(), deployDefinition);

    deploy();
    waitStartComplete();
  }

  protected void initServiceCenterCmd() throws IOException {
    // where is service center
    // 1.find from env, for local dev environment
    LOGGER.info("try to find serviceCenter by env {}.", "serviceCenterHome");
    String dir = System.getenv("serviceCenterHome");
    if (dir != null) {
      LOGGER.info("serviceCenterHome={}.", dir);
      File file = new File(dir, deployDefinition.getCmd());
      if (file.exists()) {
        FileUtils.cleanDirectory(new File(dir, "data"));
        deployDefinition.setWorkDir(dir);
        deployDefinition.setCmd(file.getAbsolutePath());
        return;
      }

      LOGGER.info("{} is not exist.", file.getAbsolutePath());
    }

    // 2.docker, for CI environment
    LOGGER.info("can not find serviceCenter by env {}, try run by docker.", "serviceCenterHome");
    deployDefinition.setCmd("docker");
    deployDefinition.setArgs(new String[] {
        "run",
        "-p",
        "127.0.0.1:30100:30100",
        "servicecomb/service-center"
    });
  }
}
