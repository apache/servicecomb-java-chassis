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

package org.apache.servicecomb.core.transport;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.core.Vertx;

public abstract class AbstractTransport implements Transport {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransport.class);

  public static final String PROP_ROOT = "servicecomb.request";

  public static final String PROP_TIMEOUT = ".timeout";

  /*
   * 用于参数传递：比如向RestServerVerticle传递endpoint地址。
   */
  public static final String ENDPOINT_KEY = "servicecomb.endpoint";

  private static final long REQUEST_TIMEOUT_CFG_FAIL = -1;

  private static final long DEFAULT_TIMEOUT_MILLIS = 30000;

  // 所有transport使用同一个vertx实例，避免创建太多的线程
  public static TransportVertxFactory transportVertxFactory = new TransportVertxFactory();

  protected Vertx transportVertx = transportVertxFactory.getTransportVertx();

  protected Endpoint endpoint;

  protected Endpoint publishEndpoint;

  private final AtomicInteger connectedCounter = new AtomicInteger(0);

  @Override
  public Endpoint getPublishEndpoint() {
    return publishEndpoint;
  }

  @Override
  public Endpoint getEndpoint() {
    return endpoint;
  }

  @Override
  public AtomicInteger getConnectedCounter() {
    return connectedCounter;
  }

  protected void setListenAddressWithoutSchema(String addressWithoutSchema) {
    setListenAddressWithoutSchema(addressWithoutSchema, null);
  }

  /*
   * 将配置的URI转换为endpoint
   * addressWithoutSchema 配置的URI，没有schema部分
   */
  protected void setListenAddressWithoutSchema(String addressWithoutSchema,
      Map<String, String> pairs) {
    addressWithoutSchema = genAddressWithoutSchema(addressWithoutSchema, pairs);

    this.endpoint = new Endpoint(this, NetUtils.getRealListenAddress(getName(), addressWithoutSchema));
    if (this.endpoint.getEndpoint() != null) {
      this.publishEndpoint = new Endpoint(this, RegistryUtils.getPublishAddress(getName(),
          addressWithoutSchema));
    } else {
      this.publishEndpoint = null;
    }
  }

  private String genAddressWithoutSchema(String addressWithoutSchema, Map<String, String> pairs) {
    if (addressWithoutSchema == null || pairs == null || pairs.isEmpty()) {
      return addressWithoutSchema;
    }

    int idx = addressWithoutSchema.indexOf('?');
    if (idx == -1) {
      addressWithoutSchema += "?";
    } else {
      addressWithoutSchema += "&";
    }

    String encodedQuery = URLEncodedUtils.format(pairs.entrySet().stream().map(entry -> {
      return new BasicNameValuePair(entry.getKey(), entry.getValue());
    }).collect(Collectors.toList()), StandardCharsets.UTF_8.name());

    if (!RegistryUtils.getServiceRegistry().getFeatures().isCanEncodeEndpoint()) {
      addressWithoutSchema = genAddressWithoutSchemaForOldSC(addressWithoutSchema, encodedQuery);
    } else {
      addressWithoutSchema += encodedQuery;
    }

    return addressWithoutSchema;
  }

  private String genAddressWithoutSchemaForOldSC(String addressWithoutSchema, String encodedQuery) {
    // old service center do not support encodedQuery
    // sdk must query service center's version, and determine if encode query
    // traced by JAV-307
    try {
      LOGGER.warn("Service center do not support encoded query, so we use unencoded query, "
          + "this caused not support chinese/space (and maybe other char) in query value.");
      String decodedQuery = URLDecoder.decode(encodedQuery, StandardCharsets.UTF_8.name());
      addressWithoutSchema += decodedQuery;
    } catch (UnsupportedEncodingException e) {
      // never happened
      throw new ServiceCombException("Failed to decode query.", e);
    }

    try {
      // make sure consumer can handle this endpoint
      new URI(Const.RESTFUL + "://" + addressWithoutSchema);
    } catch (URISyntaxException e) {
      throw new ServiceCombException(
          "current service center not support encoded endpoint, please do not use chinese or space or anything need to be encoded.",
          e);
    }
    return addressWithoutSchema;
  }

  @Override
  public Object parseAddress(String address) {
    if (address == null) {
      return null;
    }
    return new URIEndpointObject(address);
  }

  /**
   * Handles the request timeout configurations. 
   * @param operationName operation name
   * @param schema schema id
   * @param microservice micro service name
   * @return configured value
   */
  public static long getReqTimeout(String operationName, String schema, String microservice) {
    return getLongProperty(DEFAULT_TIMEOUT_MILLIS,
        PROP_ROOT + "." + microservice + "." + schema + "." + operationName + PROP_TIMEOUT,
        PROP_ROOT + "." + microservice + "." + schema + PROP_TIMEOUT,
        PROP_ROOT + "." + microservice + PROP_TIMEOUT,
        PROP_ROOT + PROP_TIMEOUT);
  }

  /**
   * Handles the request timeout configurations. 
   * @param defaultValue
   * @param keys list of keys
   * @return configured value
   */
  private static long getLongProperty(long defaultValue, String... keys) {
    for (String key : keys) {
      long property = DynamicPropertyFactory.getInstance().getLongProperty(key, REQUEST_TIMEOUT_CFG_FAIL).get();
      if (property != REQUEST_TIMEOUT_CFG_FAIL) {
        return property;
      }
    }
    return defaultValue;
  }
}
