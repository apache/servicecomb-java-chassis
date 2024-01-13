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

package org.apache.servicecomb.config.client;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.servicecomb.config.archaius.sources.ApolloConfigurationSourceImpl.UpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.servicecomb.config.client.ConfigurationAction.*;

public class ApolloClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApolloClient.class);

  private static final ApolloConfig APOLLO_CONFIG = ApolloConfig.INSTANCE;

  private static final Map<String, Object> originalConfigMap = new ConcurrentHashMap<>();

  private final String namespace = APOLLO_CONFIG.getNamespace();

  private final UpdateHandler updateHandler;


  public ApolloClient(UpdateHandler updateHandler) {
    this.updateHandler = updateHandler;
  }

  @VisibleForTesting
  static Map<String, Object> getOriginalConfigMap() {
    return originalConfigMap;
  }

  public void refreshApolloConfig() {
    String namespaces = namespace!=null?namespace:"application";
    List<String> namespaceList = Arrays.asList(namespaces.split(","));
    for (String ns : namespaceList) {
      ConfigFileFormat format =determineFileFormat(ns);
      if (format == ConfigFileFormat.YAML||format == ConfigFileFormat.Properties ||format == ConfigFileFormat.YML){
        Config config =  ConfigService.getConfig(ns);
        this.refreshConfig(config);
        config.addChangeListener(changeEvent -> {
          LOGGER.info("Changes for namespace {}" , changeEvent.getNamespace());
          this.refreshConfig(config);
          for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            String propertyName = change.getPropertyName();
            String oldValue = change.getOldValue();
            String newValue = change.getOldValue();
            PropertyChangeType changeType = change.getChangeType();
            LOGGER.info("Found change - key: {}, oldValue: {}, newValue: {}, changeType: {}", propertyName, oldValue, newValue, changeType);
          }
        });
      }else{
        ConfigFile configFile = ConfigService.getConfigFile(ns, format);
        this.refreshConfigForJSON(format, configFile.getContent());
        configFile.addChangeListener(changeEvent -> {
          LOGGER.info("Changes for namespace {}" , changeEvent.getNamespace());
          PropertyChangeType changeType = changeEvent.getChangeType();
          String newValue = changeEvent.getNewValue();
          String oldValue = changeEvent.getOldValue();
          LOGGER.info("Found change - oldValue: {}, newValue: {}, changeType: {}", oldValue, newValue, changeType);
          this.refreshConfigForJSON(format, newValue);
        });
      }
    }
  }

  private void refreshConfigForJSON(ConfigFileFormat format, String newValue) {
    if(format ==ConfigFileFormat.JSON ){
      Map<String, Object> newConfig = this.parseJsonToFirstLevel(newValue);
      this.refreshConfigItems(newConfig);
    }else{
      Map<String, Object> newConfig = new HashMap<>();
      newConfig.put("content", newValue);
      this.refreshConfigItems(newConfig);
    }
  }

  private void refreshConfig(Config config) {
    Map<String, Object> newConfig = new LinkedHashMap<>();
    config.getPropertyNames().forEach(key ->newConfig.put(key, config.getProperty(key,null)));
    this.refreshConfigItems(newConfig);
  }


  private ConfigFileFormat determineFileFormat(String namespaceName) {
    String lowerCase = namespaceName.toLowerCase();
    ConfigFileFormat format = Arrays.stream(ConfigFileFormat.values()).filter(o-> lowerCase.endsWith("." + o.getValue())).findFirst().get();
    if(format==null){
      return ConfigFileFormat.Properties;
    }
    return format;
  }

  private Map<String, Object> parseJsonToFirstLevel(String json) {
    Map<String, Object> resultMap = new HashMap<>();
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      JsonNode rootNode = objectMapper.readTree(json);
      Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
      while (fieldsIterator.hasNext()) {
        Map.Entry<String, JsonNode> field = fieldsIterator.next();
        if (field.getValue().isObject()) {
          resultMap.put(field.getKey(), field.getValue().asText());
        } else {
          resultMap.put(field.getKey(), field.getValue().asText());
        }
      }
    } catch (IOException e) {
      LOGGER.error("JsonObject parse config center response error: ", e);
    }
    return resultMap;
  }

  private void refreshConfigItems(Map<String, Object> map) {
    this.compareChangedConfig(originalConfigMap, map);
    originalConfigMap.clear();
    originalConfigMap.putAll(map);
  }

  private void compareChangedConfig(Map<String, Object> before, Map<String, Object> after) {
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
    after.forEach((itemKey, itemValue) -> {
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

