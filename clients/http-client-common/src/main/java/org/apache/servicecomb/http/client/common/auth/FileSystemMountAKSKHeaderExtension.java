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

package org.apache.servicecomb.http.client.common.auth;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.servicecomb.http.client.common.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class FileSystemMountAKSKHeaderExtension extends AKSKHeaderExtension {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemMountAKSKHeaderExtension.class);

  private ExecutorService executor = Executors.newFixedThreadPool(1);

  public FileSystemMountAKSKHeaderExtension() {
    try {
      Path p = Paths.get(DEFAULT_SECRET_AUTH_PATH);
      if (!p.toFile().exists()) {
        return;
      }
      WatchService watchService = FileSystems.getDefault().newWatchService();
      p.register(watchService,
          StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_CREATE);
      executor.execute(new FileUpdateCheckThread(watchService));
    } catch (Exception e) {
      LOGGER.warn("get watch service failed.", e);
    }
  }

  @Override
  public void createAuthHeaders() {
    try {
      String content = new String(
          Files.readAllBytes(Paths.get(DEFAULT_SECRET_AUTH_PATH, DEFAULT_SECRET_AUTH_NAME)),
          "UTF-8");
      JsonNode data = HttpUtils.readTree(content);
      JsonNode authNode = data.findValue("auth");
      decode(authNode);
    } catch (Exception e) {
      LOGGER.warn("read auth info from dockerconfigjson failed.", e);
    }
  }


  final class FileUpdateCheckThread implements Runnable {

    private WatchService service;

    private FileUpdateCheckThread(WatchService service) {
      this.service = service;
    }

    public void run() {
      while (true) {
        try {
          WatchKey watchKey = service.take();
          // 清理掉已发生的事件，否则会导致事件遗留，进入死循环
          watchKey.pollEvents();
          synchronized (this) {
            createAuthHeaders();
          }
          watchKey.reset();
        } catch (InterruptedException e) {
          LOGGER.error("error occured. detail : {}", e.getMessage());
        }
      }
    }
  }
}
