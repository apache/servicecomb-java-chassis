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

import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.config.kie.client.exception.OperationException;
import org.apache.servicecomb.config.kie.client.model.ConfigConstants;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsResponse;
import org.apache.servicecomb.config.kie.client.model.KVDoc;
import org.apache.servicecomb.config.kie.client.model.KVResponse;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.apache.servicecomb.config.kie.client.model.ValueType;
import org.apache.servicecomb.http.client.common.HttpRequest;
import org.apache.servicecomb.http.client.common.HttpResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ByteArrayResource;

public class KieClient implements KieConfigOperation {

  private static final Logger LOGGER = LoggerFactory.getLogger(KieClient.class);

  protected HttpTransport httpTransport;

  protected String revision = "0";

  private final KieAddressManager addressManager;

  private final KieConfiguration kieConfiguration;

  public static final String DEFAULT_KIE_API_VERSION = "v1";

  public KieClient(KieAddressManager addressManager, HttpTransport httpTransport, KieConfiguration kieConfiguration) {
    this.httpTransport = httpTransport;
    this.addressManager = addressManager;
    this.kieConfiguration = kieConfiguration;
  }

  @Override
  public ConfigurationsResponse queryConfigurations(ConfigurationsRequest request) {
    try {
      String url = addressManager.address()
          + "/"
          + DEFAULT_KIE_API_VERSION
          + "/"
          + kieConfiguration.getProject()
          + "/kie/kv?"
          + request.getLabelsQuery()
          + "&revision="
          + request.getRevision()
          + "&withExact="
          + request.isWithExact();

      if (kieConfiguration.isEnableLongPolling()) {
        url += "&wait=" + kieConfiguration.getPollingWaitInSeconds() + "s";
      }

      HttpRequest httpRequest = new HttpRequest(url, null, null, HttpRequest.GET);
      HttpResponse httpResponse = httpTransport.doRequest(httpRequest);
      ConfigurationsResponse configurationsResponse = new ConfigurationsResponse();
      if (httpResponse.getStatusCode() == HttpStatus.SC_OK) {
        revision = httpResponse.getHeader("X-Kie-Revision");
        KVResponse allConfigList = HttpUtils.deserialize(httpResponse.getContent(), KVResponse.class);
        Map<String, Object> configurations = getConfigByLabel(allConfigList);
        configurationsResponse.setConfigurations(configurations);
        configurationsResponse.setChanged(true);
        configurationsResponse.setRevision(revision);
        return configurationsResponse;
      }
      if (httpResponse.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
        throw new OperationException("Bad request for query configurations.");
      }
      if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_MODIFIED) {
        configurationsResponse.setChanged(false);
        return configurationsResponse;
      }
      throw new OperationException(
          "read response failed. status:" + httpResponse.getStatusCode() + "; message:" +
              httpResponse.getMessage() + "; content:" + httpResponse.getContent());
    } catch (Exception e) {
      addressManager.nextAddress();
      throw new OperationException("read response failed. ", e);
    }
  }

  private Map<String, Object> getConfigByLabel(KVResponse resp) {
    Map<String, Object> resultMap = new HashMap<>();
    resp.getData().stream()
        .filter(doc -> doc.getStatus() == null || ConfigConstants.STATUS_ENABLED.equalsIgnoreCase(doc.getStatus()))
        .map(this::processValueType)
        .collect(Collectors.toList())
        .forEach(resultMap::putAll);
    return resultMap;
  }

  private Map<String, Object> processValueType(KVDoc kvDoc) {
    ValueType valueType;
    try {
      valueType = ValueType.valueOf(kvDoc.getValueType());
    } catch (IllegalArgumentException e) {
      throw new OperationException("value type not support");
    }
    Properties properties = new Properties();
    Map<String, Object> kvMap = new HashMap<>();
    try {
      switch (valueType) {
        case yml:
        case yaml:
          YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
          yamlFactory.setResources(new ByteArrayResource(kvDoc.getValue().getBytes()));
          return toMap(yamlFactory.getObject());
        case properties:
          properties.load(new StringReader(kvDoc.getValue()));
          return toMap(properties);
        case text:
        case string:
        default:
          kvMap.put(kvDoc.getKey(), kvDoc.getValue());
          return kvMap;
      }
    } catch (Exception e) {
      LOGGER.error("read config failed");
    }
    return Collections.emptyMap();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> toMap(Properties properties) {
    if (properties == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> result = new HashMap<>();
    Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      Object value = properties.getProperty(key);
      result.put(key, value);
    }
    return result;
  }
}
