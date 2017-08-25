/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core.handler.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import io.servicecomb.core.Handler;

@JacksonXmlRootElement
public class Config {
  // key为handler id
  private Map<String, Class<Handler>> handlerClassMap = new HashMap<>();

  public void mergeFrom(Config otherConfig) {
    handlerClassMap.putAll(otherConfig.handlerClassMap);
  }

  public Map<String, Class<Handler>> getHandlerClassMap() {
    return this.handlerClassMap;
  }

  @JacksonXmlProperty(localName = "handler")
  @JacksonXmlElementWrapper(useWrapping = false)
  public void setHandlerConfigList(List<HandlerConfig> handlerConfigList) {
    for (HandlerConfig handlerConfig : handlerConfigList) {
      handlerClassMap.put(handlerConfig.getHandlerId(), handlerConfig.getClazz());
    }
  }
}
