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

package org.apache.servicecomb.config.nacos.client;

import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.CREATE;
import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.DELETE;
import static org.apache.servicecomb.config.nacos.client.ConfigurationAction.SET;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.nacos.archaius.sources.NacosConfigurationSourceImpl.UpdateHandler;
import org.apache.servicecomb.config.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;

public class NacosClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(NacosClient.class);

  private static final Map<String, Object> originalConfigMap = new ConcurrentHashMap<>();

  private final UpdateHandler updateHandler;

  public NacosClient(UpdateHandler updateHandler) {
    this.updateHandler = updateHandler;
  }

  public void refreshNacosConfig() {
    new ConfigRefresh().refreshConfig();
  }

  class ConfigRefresh {
    Parser contentParser = Parser.findParser(NacosConfig.INSTANCE.getContentType());

    String keyPrefix = NacosConfig.INSTANCE.getGroup() + "." +
        NacosConfig.INSTANCE.getDataId();

    ConfigRefresh() {
    }

    @SuppressWarnings("unchecked")
    void refreshConfig() {
      Properties properties = new Properties();
      properties.put("serverAddr", NacosConfig.INSTANCE.getServerAddr());
      properties.put("namespace", NacosConfig.INSTANCE.getNameSpace());

      try {
        ConfigService configService = NacosFactory.createConfigService(properties);
        String content = configService.getConfig(NacosConfig.INSTANCE.getDataId(),
            NacosConfig.INSTANCE.getGroup(), 5000);
        processContent(content);
        configService.addListener(NacosConfig.INSTANCE.getDataId(),
            NacosConfig.INSTANCE.getGroup(), new Listener() {
              @Override
              public void receiveConfigInfo(String configInfo) {
                LOGGER.info("receive from nacos:" + configInfo);
                processContent(configInfo);
              }

              @Override
              public Executor getExecutor() {
                return null;
              }
            });
      } catch (Exception e) {
        LOGGER.error("Receive nacos config error: ", e);
      }
    }

    private void processContent(String content) {
      if (StringUtils.isEmpty(content)) {
        return;
      }

      refreshConfigItems(contentParser.parse(content, keyPrefix, NacosConfig.INSTANCE.getAddPrefix()));
    }

    private void refreshConfigItems(Map<String, Object> map) {
      compareChangedConfig(originalConfigMap, map);
      originalConfigMap.clear();
      originalConfigMap.putAll(map);
    }

    void compareChangedConfig(Map<String, Object> before, Map<String, Object> after) {
      Map<String, Object> itemsCreated = new HashMap<>();
      Map<String, Object> itemsDeleted = new HashMap<>();
      Map<String, Object> itemsModified = new HashMap<>();
      if (before == null || before.isEmpty()) {
        updateHandler.handle(CREATE, after);
        return;
      }
      if (after == null || after.isEmpty()) {
        updateHandler.handle(DELETE, before);
        return;
      }
      after.entrySet().forEach(stringObjectEntry -> {
        String itemKey = stringObjectEntry.getKey();
        Object itemValue = stringObjectEntry.getValue();
        if (!before.containsKey(itemKey)) {
          itemsCreated.put(itemKey, itemValue);
        } else if (!itemValue.equals(before.get(itemKey))) {
          itemsModified.put(itemKey, itemValue);
        }
      });
      for (String itemKey : before.keySet()) {
        if (!after.containsKey(itemKey)) {
          itemsDeleted.put(itemKey, "");
        }
      }
      updateHandler.handle(CREATE, itemsCreated);
      updateHandler.handle(SET, itemsModified);
      updateHandler.handle(DELETE, itemsDeleted);
    }
  }
}

