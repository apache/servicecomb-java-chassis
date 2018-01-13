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

package org.apache.servicecomb.core.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.handler.config.Config;
import org.apache.servicecomb.foundation.common.AbstractObjectManager;
import org.springframework.util.StringUtils;

import com.netflix.config.DynamicPropertyFactory;

// key为microserviceName
public abstract class AbstractHandlerManager extends AbstractObjectManager<String, String, List<Handler>> {

  private String defaultChainDef;

  private Config config;

  protected abstract String getName();

  // consumer、provider端，最后一个handler，都是由框架指定的，业务不可配置
  protected abstract Handler getLastHandler();

  //  内置默认值，用于业务未指定时的取值
  protected abstract String getInnerDefaultChainDef();

  private void loadDefaultChainDef() {
    String key = "cse.handler.chain." + getName() + ".default";

    defaultChainDef = DynamicPropertyFactory.getInstance()
        .getStringProperty(key, getInnerDefaultChainDef())
        .get();
  }

  private List<Class<Handler>> convertToChainClass(String chainDef) {
    List<Class<Handler>> result = new ArrayList<>();
    if (StringUtils.isEmpty(chainDef)) {
      return result;
    }

    String[] handlerIds = chainDef.split(",");
    Map<String, Class<Handler>> handlerMaps = config.getHandlerClassMap();
    for (String handlerId : handlerIds) {
      if (handlerId != null) {
        handlerId = handlerId.trim();
      }
      if (StringUtils.isEmpty(handlerId)) {
        continue;
      }

      Class<Handler> cls = handlerMaps.get(handlerId);
      if (cls == null) {
        throw new Error("can not find handler :" + handlerId);
      }
      result.add(cls);
    }
    return result;
  }

  public void init(Config config) {
    this.config = config;

    loadDefaultChainDef();
  }

  private List<Handler> createHandlerChain(String chainDef) {
    List<Class<Handler>> chainClasses = convertToChainClass(chainDef);

    List<Handler> handlerList = new ArrayList<>();
    handlerList.add(ShutdownHookHandler.INSTANCE);

    for (Class<Handler> cls : chainClasses) {
      try {
        handlerList.add(cls.newInstance());
      } catch (Exception e) {
        // 在启动阶段直接抛异常出来
        throw new Error(e);
      }
    }
    handlerList.add(getLastHandler());
    return handlerList;
  }

  @Override
  protected String getKey(String microserviceName) {
    return microserviceName;
  }

  @Override
  protected List<Handler> create(String microserviceName) {
    String chainDef = DynamicPropertyFactory.getInstance()
        .getStringProperty("cse.handler.chain." + getName() + ".service." + microserviceName,
            defaultChainDef)
        .get();
    return createHandlerChain(chainDef);
  }
}
