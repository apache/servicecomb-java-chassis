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

  private static LinkedHashMap<String, Map<String, Object>> multiDimensionItems = new LinkedHashMap<>();

  public static final Map<String, Object> flatItems = new HashMap<>();

  private UpdateHandler updateHandler;

  public ParseConfigUtils(UpdateHandler updateHandler) {
    this.updateHandler = updateHandler;
  }

  public void refreshConfigItems(Map<String, Map<String, Object>> remoteItems) {
    multiDimensionItems.clear();
    multiDimensionItems.putAll(remoteItems);
    doRefreshItems();
    LOGGER.debug("refresh config success");
  }

  public void refreshConfigItemsIncremental(Map<String, Object> action) {
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
