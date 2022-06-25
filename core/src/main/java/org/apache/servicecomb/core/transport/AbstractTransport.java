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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.SharedVertxFactory;
import org.apache.servicecomb.registry.RegistrationManager;

import io.vertx.core.Vertx;

public abstract class AbstractTransport implements Transport {

  /*
   * 用于参数传递：比如向RestServerVerticle传递endpoint地址。
   */
  public static final String ENDPOINT_KEY = "servicecomb.endpoint";

  protected Vertx transportVertx = SharedVertxFactory.getSharedVertx();

  protected Endpoint endpoint;

  protected Endpoint publishEndpoint;

  @Override
  public Endpoint getPublishEndpoint() {
    return publishEndpoint;
  }

  @Override
  public Endpoint getEndpoint() {
    return endpoint;
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
      this.publishEndpoint = new Endpoint(this, RegistrationManager.getPublishAddress(getName(),
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

    String encodedQuery = URLEncodedUtils.format(pairs.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList()), StandardCharsets.UTF_8.name());

    addressWithoutSchema += encodedQuery;

    return addressWithoutSchema;
  }

  @Override
  public Object parseAddress(String address) {
    if (address == null) {
      return null;
    }
    return new URIEndpointObject(address);
  }
}
