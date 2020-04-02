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

package org.apache.servicecomb.common.rest.codec.produce;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.foundation.common.RegisterManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProduceProcessorManager extends RegisterManager<String, Map<String, ProduceProcessor>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProduceProcessorManager.class);

  private static final List<ProduceProcessor> produceProcessor =
      SPIServiceUtils.getSortedService(ProduceProcessor.class);

  private static final String NAME = "produce processor mgr";

  public static final String DEFAULT_TYPE = MediaType.APPLICATION_JSON;

  public static final String DEFAULT_SERIAL_CLASS = "servicecomb_default_class";

  public static final ProduceProcessorManager INSTANCE = new ProduceProcessorManager();

  private Map<String, ProduceProcessor> jsonProcessorMap;

  private Map<String, ProduceProcessor> plainProcessorMap;

  private Map<String, ProduceProcessor> defaultProcessorMap;

  private ProduceProcessorManager() {
    super(NAME);
    produceProcessor.forEach(processor -> {
      Map<String, ProduceProcessor> prodProcessorMap = getObjMap()
          .computeIfAbsent(processor.getName(), key -> new HashMap<>());
      prodProcessorMap.putIfAbsent(processor.getSerializationView(), processor);
    });
    jsonProcessorMap = ensureFindValue(MediaType.APPLICATION_JSON);
    plainProcessorMap = ensureFindValue(MediaType.TEXT_PLAIN);
    defaultProcessorMap = jsonProcessorMap;
  }

  public static ProduceProcessor cloneNewProduceProcessor(String acceptType, Class<?> serialViewClass,
      Map<String, ProduceProcessor> produceViewMap) {
    ProduceProcessor newInstance;
    try {
      newInstance = produceViewMap.get(DEFAULT_SERIAL_CLASS).getClass().newInstance();
      newInstance.setSerializationView(serialViewClass);
      return newInstance;
    } catch (Throwable e) {
      LOGGER.error(String.format("Failed to create produceProcessor with %s", acceptType), e);
    }
    return produceViewMap.get(DEFAULT_SERIAL_CLASS);
  }

  public Map<String, ProduceProcessor> getJsonProcessorMap() {
    return jsonProcessorMap;
  }

  public ProduceProcessorManager setJsonProcessorMap(
      Map<String, ProduceProcessor> jsonProcessorMap) {
    this.jsonProcessorMap = jsonProcessorMap;
    return this;
  }

  public Map<String, ProduceProcessor> getPlainProcessorMap() {
    return plainProcessorMap;
  }

  public ProduceProcessorManager setPlainProcessorMap(
      Map<String, ProduceProcessor> plainProcessorMap) {
    this.plainProcessorMap = plainProcessorMap;
    return this;
  }

  public Map<String, ProduceProcessor> getDefaultProcessorMap() {
    return defaultProcessorMap;
  }

  public ProduceProcessorManager setDefaultProcessorMap(
      Map<String, ProduceProcessor> defaultProcessorMap) {
    this.defaultProcessorMap = defaultProcessorMap;
    return this;
  }
}
