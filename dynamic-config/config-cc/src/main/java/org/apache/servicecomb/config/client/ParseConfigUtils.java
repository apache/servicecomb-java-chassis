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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.servicecomb.config.archaius.sources.ConfigCenterConfigurationSourceImpl.UpdateHandler;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Created by on 2017/1/5.
 */
public class ParseConfigUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ParseConfigUtils.class);

  private static final ParseConfigUtils INSTANCE = new ParseConfigUtils();

  private LinkedHashMap<String, Map<String, Object>> multiDimensionItems = new LinkedHashMap<>();

  //it's dangerous to make flatItems public
  private final Map<String, Object> flatItems = new HashMap<>();

  private String currentVersionInfo = "default";

  private UpdateHandler updateHandler;

  private Lock configLock = new ReentrantLock();

  //for compatibility with other modules and JunitTest
  public ParseConfigUtils(UpdateHandler updateHandler) {
    this.updateHandler = updateHandler;
  }

  private ParseConfigUtils() {
  }

  public void initWithUpdateHandler(UpdateHandler updateHandler) {
    if (updateHandler == null) {
      LOGGER.error("when init ParseConfigUtils, updateHandler can not be null");
    }
    this.updateHandler = updateHandler;
  }

  /*
      as the data is returned, we can block the thread at a short time. consider that if the multiple verticles were deployed
      and if we use pull mode and push mode at the same time , we must share a common lock with all methods which would
      change the config setting
     */
  public void refreshConfigItems(Map<String, Map<String, Object>> remoteItems) {
    try {
      configLock.lock();
      String oldRevision = currentVersionInfo;
      currentVersionInfo =
          remoteItems.getOrDefault("revision", new HashMap<>()).getOrDefault("version", "default").toString();
      //make sure the currentVersionInfo != ""
      currentVersionInfo = currentVersionInfo.equals("") ? "default" : currentVersionInfo;
      //the key revision is not the config setting
      boolean newVersion = (remoteItems.remove("revision") != null);
      multiDimensionItems.clear();
      multiDimensionItems.putAll(remoteItems);
      doRefreshItems();
      if (newVersion) {
        LOGGER.info("Updating remote config is done. revision has changed from {} to {}", oldRevision,
            currentVersionInfo);
      }
    } finally {
      configLock.unlock();
    }
  }

  public static ParseConfigUtils getInstance() {
    return INSTANCE;
  }

  public String getCurrentVersionInfo() {
    return this.currentVersionInfo;
  }

  public void refreshConfigItemsIncremental(Map<String, Object> action) {
    try {
      configLock.lock();
      if ("UPDATE".equals(action.get("action"))) {
        try {
          multiDimensionItems.put((String) action.get("key"), JsonUtils.OBJ_MAPPER
              .readValue(action.get("value").toString(), new TypeReference<Map<String, Object>>() {
              }));
        } catch (IOException e) {
          LOGGER.error("parse config change action fail");
        }
        doRefreshItems();
      } else if ("DELETE".equals(action.get("action"))) {
        multiDimensionItems.remove(action.get("key"));
        doRefreshItems();
      }
    } finally {
      configLock.unlock();
    }
  }

  private void doRefreshItems() {
    Map<String, Object> freshFlatItems = mergeDimensionItems(multiDimensionItems);
    notifyItemsChangedNeedRefresh(flatItems, freshFlatItems);
    flatItems.clear();
    flatItems.putAll(freshFlatItems);
  }

  private Map<String, Object> mergeDimensionItems(Map<String, Map<String, Object>> items) {
    Map<String, Object> flatMap = new HashMap<>();
    return items.values().stream().reduce(flatMap, (result, item) -> {
      result.putAll(item);
      return result;
    });
  }

  private void notifyItemsChangedNeedRefresh(Map<String, Object> before, Map<String, Object> after) {
    Map<String, Object> itemsCreated = new HashMap<>();
    Map<String, Object> itemsDeleted = new HashMap<>();
    Map<String, Object> itemsModified = new HashMap<>();
    if (before == null || before.isEmpty()) {
      updateHandler.handle("create", after);
      return;
    }
    if (after == null || after.isEmpty()) {
      updateHandler.handle("delete", before);
      return;
    }
    for (String itemKey : after.keySet()) {
      if (!before.containsKey(itemKey)) {
        itemsCreated.put(itemKey, after.get(itemKey));
      } else if (!after.get(itemKey).equals(before.get(itemKey))) {
        itemsModified.put(itemKey, after.get(itemKey));
      }
    }
    for (String itemKey : before.keySet()) {
      if (!after.containsKey(itemKey)) {
        itemsDeleted.put(itemKey, "");
      }
    }
    updateHandler.handle("create", itemsCreated);
    updateHandler.handle("set", itemsModified);
    updateHandler.handle("delete", itemsDeleted);
  }
}
