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

import org.apache.http.entity.ContentType;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.RegisterManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import jakarta.ws.rs.core.MediaType;

public final class ProduceProcessorManager extends RegisterManager<String, Map<String, ProduceProcessor>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProduceProcessorManager.class);

  private static final List<ProduceProcessor> produceProcessor =
      SPIServiceUtils.getSortedService(ProduceProcessor.class);

  private static final String NAME = "produce processor mgr";

  public static final String DEFAULT_SERIAL_CLASS = "servicecomb_default_class";

  public static final ProduceProcessorManager INSTANCE = new ProduceProcessorManager();

  private final Map<String, ProduceProcessor> jsonProcessorMap;

  private final Map<String, ProduceProcessor> plainProcessorMap;

  private final Map<String, ProduceProcessor> defaultProcessorMap;

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

  private static ProduceProcessor cloneNewProduceProcessor(Class<?> serialViewClass,
      Map<String, ProduceProcessor> produceViewMap) {
    ProduceProcessor newInstance;
    try {
      newInstance = produceViewMap.get(DEFAULT_SERIAL_CLASS).getClass().getDeclaredConstructor().newInstance();
      newInstance.setSerializationView(serialViewClass);
      return newInstance;
    } catch (Throwable e) {
      // ignore exception
      LOGGER.warn("Failed to create produceProcessor with {}", serialViewClass.getName(), e);
    }
    return produceViewMap.get(DEFAULT_SERIAL_CLASS);
  }

  public ProduceProcessor findJsonProcessorByViewClass(Class<?> serialViewClass) {
    if (serialViewClass == null) {
      return jsonProcessorMap.get(DEFAULT_SERIAL_CLASS);
    }
    return jsonProcessorMap.computeIfAbsent(serialViewClass.getName(),
        viewKey -> cloneNewProduceProcessor(serialViewClass, jsonProcessorMap));
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

  public ProduceProcessor createProduceProcessor(OperationMeta operationMeta,
      int statusCode, String accept, Class<?> serialViewClass) {
    ApiResponses responses = operationMeta.getSwaggerOperation().getResponses();
    ApiResponse response = responses.get(String.valueOf(statusCode));
    if (response == null || response.getContent() == null ||
        response.getContent().size() == 0) {
      return findDefaultProcessor();
    }
    String actualAccept = accept;
    if (actualAccept == null) {
      if (response.getContent().get(MediaType.APPLICATION_JSON) != null) {
        actualAccept = MediaType.APPLICATION_JSON;
      } else {
        actualAccept = response.getContent().keySet().iterator().next();
      }
    }
    ContentType contentType = ContentType.parse(actualAccept);
    actualAccept = contentType.getMimeType();
    if (MediaType.WILDCARD.equals(contentType.getMimeType()) ||
        MediaType.MEDIA_TYPE_WILDCARD.equals(contentType.getMimeType())) {
      if (response.getContent().get(MediaType.APPLICATION_JSON) != null) {
        actualAccept = MediaType.APPLICATION_JSON;
      } else {
        actualAccept = response.getContent().keySet().iterator().next();
      }
    }
    if (response.getContent().get(actualAccept) == null) {
      LOGGER.warn("Operation do not support accept type {}/{}", accept, actualAccept);
      return findDefaultProcessor();
    }
    if (SwaggerConst.PROTOBUF_TYPE.equals(actualAccept)) {
      return new ProduceProtoBufferProcessor(operationMeta,
          operationMeta.getSchemaMeta().getSwagger(), response.getContent().get(actualAccept).getSchema());
    }
    if (MediaType.TEXT_PLAIN.equals(actualAccept)) {
      return findPlainProcessorByViewClass(serialViewClass);
    }
    // json
    return findJsonProcessorByViewClass(serialViewClass);
  }
}
