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

import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.vertx.SharedVertxFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import io.vertx.core.Vertx;

public abstract class AbstractTransport implements Transport {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransport.class);

  public static final String PUBLISH_ADDRESS = "servicecomb.service.publishAddress";

  private static final String PUBLISH_PORT = "servicecomb.{transport_name}.publishPort";

  /*
   * 用于参数传递：比如向RestServerVerticle传递endpoint地址。
   */
  public static final String ENDPOINT_KEY = "servicecomb.endpoint";

  protected Vertx transportVertx = SharedVertxFactory.getSharedVertx();

  protected Endpoint endpoint;

  protected Endpoint publishEndpoint;

  protected Environment environment;

  @Override
  public Endpoint getPublishEndpoint() {
    return publishEndpoint;
  }

  @Override
  public Endpoint getEndpoint() {
    return endpoint;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
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
      this.publishEndpoint = new Endpoint(this, getPublishAddress(getName(),
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

    String encodedQuery = URLEncodedUtils.format(
        pairs.entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList()), StandardCharsets.UTF_8.name());

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

  /**
   * In the case that listening address configured as 0.0.0.0, the publish address will be determined
   * by the query result for the net interfaces.
   *
   * @return the publish address, or {@code null} if the param {@code address} is null.
   */
  protected String getPublishAddress(String schema, String address) {
    if (address == null) {
      return address;
    }

    try {
      URI originalURI = new URI(schema + "://" + address);
      IpPort ipPort = NetUtils.parseIpPort(originalURI);
      if (ipPort == null) {
        LOGGER.warn("address {} not valid.", address);
        return null;
      }

      IpPort publishIpPort = genPublishIpPort(schema, ipPort);
      URIBuilder builder = new URIBuilder(originalURI);
      return builder.setHost(publishIpPort.getHostOrIp()).setPort(publishIpPort.getPort()).build().toString();
    } catch (URISyntaxException e) {
      LOGGER.warn("address {} not valid.", address);
      return null;
    }
  }

  private IpPort genPublishIpPort(String schema, IpPort ipPort) {
    String publicAddressSetting = environment.getProperty(PUBLISH_ADDRESS, String.class, "");
    publicAddressSetting = publicAddressSetting.trim();

    String publishPortKey = PUBLISH_PORT.replace("{transport_name}", schema);
    int publishPortSetting = environment.getProperty(publishPortKey, int.class, 0);
    int publishPort = publishPortSetting == 0 ? ipPort.getPort() : publishPortSetting;

    if (publicAddressSetting.isEmpty()) {
      InetSocketAddress socketAddress = ipPort.getSocketAddress();
      if (socketAddress.getAddress().isAnyLocalAddress()) {
        String host = NetUtils.getHostAddress();
        if (Inet6Address.class.isInstance(socketAddress.getAddress())) {
          host = NetUtils.getIpv6HostAddress();
        }
        LOGGER.warn("address {}, auto select a host address to publish {}:{}, maybe not the correct one",
            socketAddress,
            host,
            publishPort);
        return new IpPort(host, publishPort);
      }

      return ipPort;
    }

    if (publicAddressSetting.startsWith("{") && publicAddressSetting.endsWith("}")) {
      publicAddressSetting = NetUtils
          .ensureGetInterfaceAddress(
              publicAddressSetting.substring(1, publicAddressSetting.length() - 1))
          .getHostAddress();
    }

    return new IpPort(publicAddressSetting, publishPort);
  }
}
