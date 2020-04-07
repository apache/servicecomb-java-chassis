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
import org.springframework.util.CollectionUtils;

public final class ProduceProcessorManager extends RegisterManager<String, Map<String, ProduceProcessor>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProduceProcessorManager.class);

  private static final List<ProduceProcessor> produceProcessor =
      SPIServiceUtils.getSortedService(ProduceProcessor.class);

  private static final String NAME = "produce processor mgr";

  public static final String DEFAULT_TYPE = MediaType.APPLICATION_JSON;

  public static final String DEFAULT_SERIAL_CLASS = "servicecomb_default_class";

  public static final ProduceProcessorManager INSTANCE = new ProduceProcessorManager();

  private Map<String, ProduceProcessor> nonSerialViewMap = new HashMap<>();

  private Map<String, ProduceProcessor> jsonProcessorMap;

  private Map<String, ProduceProcessor> plainProcessorMap;

  private Map<String, ProduceProcessor> defaultProcessorMap;

  private ProduceProcessorManager() {
    super(NAME);
    produceProcessor.forEach(processor -> {
      nonSerialViewMap.put(processor.getName(), processor);
      Map<String, ProduceProcessor> prodProcessorMap = getObjMap()
          .computeIfAbsent(processor.getName(), key -> new HashMap<>());
      prodProcessorMap.putIfAbsent(processor.getSerializationView(), processor);
    });
    jsonProcessorMap = ensureFindValue(MediaType.APPLICATION_JSON);
    plainProcessorMap = ensureFindValue(MediaType.TEXT_PLAIN);
    defaultProcessorMap = jsonProcessorMap;
  }

  private static ProduceProcessor cloneNewProduceProcessor(Class<?> serialViewClass,
      Map<String, ProduceProcessor> produceViewMap) {
    ProduceProcessor newInstance;
    try {
      newInstance = produceViewMap.get(DEFAULT_SERIAL_CLASS).getClass().newInstance();
      newInstance.setSerializationView(serialViewClass);
      return newInstance;
    } catch (Throwable e) {
      // ignore exception
      LOGGER.warn("Failed to create produceProcessor with {}", serialViewClass.getName(), e);
    }
    return produceViewMap.get(DEFAULT_SERIAL_CLASS);
  }

  // key -> accept type
  public Map<String, ProduceProcessor> getOrCreateAcceptMap(Class<?> serialViewClass) {
    if (serialViewClass == null) {
      return nonSerialViewMap;
    }
    Map<String, ProduceProcessor> result = new HashMap<>();
    getObjMap().forEach((acceptKey, viewMap) -> {
      ProduceProcessor produceProcessor = viewMap.computeIfAbsent(serialViewClass.getName(),
          viewKey -> cloneNewProduceProcessor(serialViewClass, viewMap));
      result.put(acceptKey, produceProcessor);
    });
    return result;
  }

  public ProduceProcessor findProcessor(String acceptType, Class<?> serialViewClass) {
    Map<String, ProduceProcessor> viewMap = findValue(acceptType);
    if (CollectionUtils.isEmpty(viewMap)) {
      return null;
    }
    if (serialViewClass == null) {
      return viewMap.get(DEFAULT_SERIAL_CLASS);
    }
    return viewMap.computeIfAbsent(serialViewClass.getName(),
        viewKey -> cloneNewProduceProcessor(serialViewClass, viewMap));
  }

  public ProduceProcessor findJsonProcessorByViewClass(Class<?> serialViewClass) {
    if (serialViewClass == null) {
      return jsonProcessorMap.get(DEFAULT_SERIAL_CLASS);
    }
    return jsonProcessorMap.computeIfAbsent(serialViewClass.getName(),
        viewKey -> cloneNewProduceProcessor(serialViewClass, jsonProcessorMap));
  }

  public ProduceProcessor findDefaultProcessorByViewClass(Class<?> serialViewClass) {
    if (serialViewClass == null) {
      return defaultProcessorMap.get(DEFAULT_SERIAL_CLASS);
    }
    return defaultProcessorMap.computeIfAbsent(serialViewClass.getName(),
        viewKey -> cloneNewProduceProcessor(serialViewClass, defaultProcessorMap));
  }

  public ProduceProcessor findPlainProcessorByViewClass(Class<?> serialViewClass) {
    if (serialViewClass == null) {
      return plainProcessorMap.get(DEFAULT_SERIAL_CLASS);
    }
    return plainProcessorMap.computeIfAbsent(serialViewClass.getName(),
        viewKey -> cloneNewProduceProcessor(serialViewClass, plainProcessorMap));
  }

  public ProduceProcessor findDefaultJsonProcessor() {
    return jsonProcessorMap.get(DEFAULT_SERIAL_CLASS);
  }

  public ProduceProcessor findDefaultProcessor() {
    return defaultProcessorMap.get(DEFAULT_SERIAL_CLASS);
  }

  public ProduceProcessor findDefaultPlainProcessor() {
    return plainProcessorMap.get(DEFAULT_SERIAL_CLASS);
  }
}
