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

package org.apache.servicecomb.config.kie.client;

import java.util.HashMap;
import java.util.Map;
import org.apache.servicecomb.config.kie.archaius.sources.KieConfigurationSourceImpl.UpdateHandler;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class KieWatcher {

  public static final KieWatcher INSTANCE = new KieWatcher();

  private UpdateHandler updateHandler;

  private String refreshRecord;

  Map<String, Object> lastTimeData;

  private KieWatcher() {
  }

  public void setUpdateHandler(UpdateHandler updateHandler) {
    this.updateHandler = updateHandler;
  }

  public void refreshConfigItems(Map<String, Object> remoteItems) {
    String md5Vaule = KieUtil.encrypt(remoteItems.toString());
    if (CollectionUtils.isEmpty(remoteItems)) {
      updateHandler.handle("delete", lastTimeData);
      lastTimeData = remoteItems;
      return;
    }
    if (StringUtils.isEmpty(refreshRecord)) {
      refreshRecord = md5Vaule;
      updateHandler.handle("create", remoteItems);
      lastTimeData = remoteItems;
      return;
    }
    if (md5Vaule.equals(refreshRecord)) {
      return;
    }
    refreshRecord = md5Vaule;
    doRefresh(remoteItems);
    lastTimeData = remoteItems;
  }


  private void doRefresh(Map<String, Object> remoteItems) {
    Map<String, Object> itemsCreated = new HashMap<>();
    Map<String, Object> itemsDeleted = new HashMap<>();
    Map<String, Object> itemsModified = new HashMap<>();
    for (String itemKey : remoteItems.keySet()) {
      if (!lastTimeData.containsKey(itemKey)) {
        itemsCreated.put(itemKey, remoteItems.get(itemKey));
      } else if (!remoteItems.get(itemKey).equals(lastTimeData.get(itemKey))) {
        itemsModified.put(itemKey, remoteItems.get(itemKey));
      }
    }
    for (String itemKey : lastTimeData.keySet()) {
      if (!remoteItems.containsKey(itemKey)) {
        itemsDeleted.put(itemKey, "");
      }
    }
    updateHandler.handle("create", itemsCreated);
    updateHandler.handle("set", itemsModified);
    updateHandler.handle("delete", itemsDeleted);
  }

}
