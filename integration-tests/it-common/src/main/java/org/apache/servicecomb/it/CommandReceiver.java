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
package org.apache.servicecomb.it;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class CommandReceiver {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandReceiver.class);

  public CommandReceiver() {
    Thread thread = new Thread(this::run, "it-command-receiver");
    thread.setDaemon(true);
    thread.start();
  }

  public void run() {
    try {
      doRun();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void doRun() throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String line;
    while ((line = reader.readLine()) != null) {
      try {
        JsonNode cmd = JsonUtils.OBJ_MAPPER.readTree(line);
        dispatchCommand(cmd);
      } catch (Throwable e) {
        LOGGER.error("Failed to execute command: {}", line, e);
      }
    }
    reader.close();
  }

  private void dispatchCommand(JsonNode cmd) {
    LOGGER.info("dispatch command: {}", cmd);
    if (TextNode.class.isInstance(cmd)) {
      onStringCommand(cmd.asText());
      return;
    }

    throw new UnsupportedOperationException(cmd.toString());
  }

  protected void onStringCommand(String command) {
    switch (command) {
      case "ms-stop":
        stop();
        break;
      default:
        throw new UnsupportedOperationException(command);
    }
  }

  protected void stop() {
    new Thread(() -> {
      for (; ; ) {
        SCBEngine.getInstance().destroy();
        if (ITBootListener.isDone() && SCBStatus.DOWN.equals(SCBEngine.getInstance().getStatus())) {
          LOGGER.info("succeed to close " + RegistryUtils.getMicroservice().getServiceName());
          break;
        }
        ITUtils.forceWait(TimeUnit.SECONDS, 1);
      }

      //make sure kill for servlet
      System.exit(0);

    }, "it-stop").start();
  }
}
