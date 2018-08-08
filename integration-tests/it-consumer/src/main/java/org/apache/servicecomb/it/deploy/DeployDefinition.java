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

import io.vertx.core.json.Json;

public class DeployDefinition {
  protected String deployName;

  protected String displayName;

  protected String startCompleteLog;

  protected String workDir;

  /**
   * <pre>
   * edge as the example:
   *  support:
   *  1.absolute path: /home/xxx/it-edge/it-edge-1.0.0.jar
   *  2.relate to work dir:
   *      if work dir is /home/xxx
   *      cmd is it-edge/it-edge-1.0.0.jar
   *  3.run in ide, cmd is it-edge/it-edge-1.0.0.jar
   *      will try: integration-tests/target/it-edge/it-edge-1.0.0.jar
   *  4.run in ide, cmd is it-edge
   *      will try: integration-tests/target/it-edge/it-edge-1.0.0.jar
   *  </pre>
   */
  protected String cmd;

  protected String[] args;

  public String getDeployName() {
    return deployName;
  }

  public void setDeployName(String deployName) {
    this.deployName = deployName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getStartCompleteLog() {
    return startCompleteLog;
  }

  public void setStartCompleteLog(String startCompleteLog) {
    this.startCompleteLog = startCompleteLog;
  }

  public String getWorkDir() {
    return workDir;
  }

  public void setWorkDir(String workDir) {
    this.workDir = workDir;
  }

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public String[] getArgs() {
    return args;
  }

  public void setArgs(String[] args) {
    this.args = args;
  }

  public void init() {
    if (displayName == null) {
      displayName = deployName;
    }
  }

  @Override
  public String toString() {
    return Json.encodePrettily(this);
  }
}
