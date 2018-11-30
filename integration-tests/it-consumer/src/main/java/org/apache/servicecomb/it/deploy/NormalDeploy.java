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


import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NormalDeploy {
  private static final Logger LOGGER = LoggerFactory.getLogger(NormalDeploy.class);

  protected DeployDefinition deployDefinition;

  protected Process subProcess;

  protected BufferedWriter subProcessCommandWriter;

  protected SubProcessLogger subProcessLogger;

  private int prevFailCount;

  public NormalDeploy(DeployDefinition deployDefinition) {
    this.deployDefinition = deployDefinition;
  }

  public void deploy() throws Throwable {
    String[] cmds = createCmds();
    cmds = addArgs(cmds);

    this.prevFailCount = ITJUnitUtils.getFailures().size();

    subProcess = createProcessBuilder(cmds).start();
    subProcessCommandWriter = new BufferedWriter(new OutputStreamWriter(subProcess.getOutputStream()));
    subProcessLogger = new SubProcessLogger(deployDefinition.getDisplayName(), subProcess.getInputStream(),
        deployDefinition.getStartCompleteLog());
  }

  protected String[] addArgs(String[] cmds) {
    cmds = ArrayUtils.addAll(cmds, deployDefinition.getArgs());
    return cmds;
  }

  protected String[] createCmds() {
    return new String[] {deployDefinition.getCmd()};
  }

  protected ProcessBuilder createProcessBuilder(String[] cmds) {
    ProcessBuilder processBuilder = new ProcessBuilder(cmds).redirectErrorStream(true);
    if (deployDefinition.getWorkDir() != null) {
      processBuilder.directory(new File(deployDefinition.getWorkDir()));
    }
    return processBuilder;
  }

  public void waitStartComplete() {
    subProcessLogger.waitStartComplete();
  }

  public void sendCommand(Object command) {
    String strCmd = null;
    try {
      strCmd = JsonUtils.writeValueAsString(command);
      subProcessCommandWriter.write(strCmd + "\n");
      subProcessCommandWriter.flush();
    } catch (Throwable e) {
      LOGGER.error("Failed to send command, displayName={}, command={}", deployDefinition.getDisplayName(), strCmd, e);
    }
  }

  protected void afterStop() {
    IOUtils.closeQuietly(subProcessCommandWriter);
    subProcessCommandWriter = null;

    SubProcessLogger old = subProcessLogger;
    IOUtils.closeQuietly(subProcessLogger);
    subProcessLogger = null;

    if (prevFailCount != ITJUnitUtils.getFailures().size()) {
      List<String> logs = old.getAndClearLog();
      for (String line : logs) {
        System.out.println(line);
      }
    }
  }

  public void waitStop() {
    if (subProcess == null) {
      LOGGER.info("Ignore, already stop or reusing exist instance, displayName={}.", deployDefinition.getDisplayName());
      return;
    }

    for (; ; ) {
      try {
        subProcess.waitFor();
        break;
      } catch (InterruptedException e) {
        LOGGER.info("Ignore InterruptedException, try to wait stop again, displayName={}.",
            deployDefinition.getDisplayName());
      }
    }
    subProcess = null;
    afterStop();

    LOGGER.info("stop complete, displayName={}.", deployDefinition.getDisplayName());
  }

  public void stop() {
    if (subProcess == null) {
      LOGGER.info("Ignore, already stop or reusing exist instance, displayName={}.", deployDefinition.getDisplayName());
      return;
    }

    subProcess.destroy();
    subProcess = null;
    afterStop();
    LOGGER.info("stop complete, displayName={}.", deployDefinition.getDisplayName());
  }

  public List<String> getAndClearLog() {
    SubProcessLogger logger = subProcessLogger;
    if (logger != null) {
      return logger.getAndClearLog();
    }

    return Collections.emptyList();
  }
}
