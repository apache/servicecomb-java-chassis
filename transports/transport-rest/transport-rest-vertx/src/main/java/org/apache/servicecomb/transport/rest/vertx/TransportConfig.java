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

package org.apache.servicecomb.transport.rest.vertx;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.transport.common.TransportConfigUtils;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import io.vertx.core.Verticle;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpServerOptions;

public final class TransportConfig {
  public static final int DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND = 60;

  public static final boolean DEFAULT_SERVER_COMPRESSION_SUPPORT = false;

  // 32K
  public static final int DEFAULT_SERVER_MAX_HEADER_SIZE = 32 * 1024;

  public static final String SERVICECOMB_CORS_CONFIG_BASE = "servicecomb.cors";

  private static Class<? extends Verticle> restServerVerticle = RestServerVerticle.class;

  private TransportConfig() {
  }

  public static Class<? extends Verticle> getRestServerVerticle() {
    return restServerVerticle;
  }

  public static void setRestServerVerticle(Class<? extends Verticle> restServerVerticle) {
    TransportConfig.restServerVerticle = restServerVerticle;
  }

  public static String getAddress() {
    DynamicStringProperty address =
        DynamicPropertyFactory.getInstance().getStringProperty("servicecomb.rest.address", null);
    return address.get();
  }

  public static int getMaxFormAttributeSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.maxFormAttributeSize",
            HttpServerOptions.DEFAULT_MAX_FORM_ATTRIBUTE_SIZE).get();
  }

  public static int getCompressionLevel() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.compressionLevel",
            HttpServerOptions.DEFAULT_COMPRESSION_LEVEL).get();
  }

  public static int getMaxChunkSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.maxChunkSize",
            HttpServerOptions.DEFAULT_MAX_CHUNK_SIZE).get();
  }

  public static int getDecoderInitialBufferSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.decoderInitialBufferSize",
            HttpServerOptions.DEFAULT_DECODER_INITIAL_BUFFER_SIZE).get();
  }

  public static int getHttp2ConnectionWindowSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.http2ConnectionWindowSize",
            HttpServerOptions.DEFAULT_HTTP2_CONNECTION_WINDOW_SIZE).get();
  }

  public static int getThreadCount() {
    return TransportConfigUtils.readVerticleCount(
        "servicecomb.rest.server.verticle-count",
        "servicecomb.rest.server.thread-count");
  }

  public static int getConnectionIdleTimeoutInSeconds() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.connection.idleTimeoutInSeconds",
            DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND)
        .get();
  }

  public static boolean getCompressed() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.server.compression", DEFAULT_SERVER_COMPRESSION_SUPPORT)
        .get();
  }

  public static boolean getDecompressionSupported() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.server.decompressionSupported",
            HttpServerOptions.DEFAULT_DECOMPRESSION_SUPPORTED).get();
  }

  public static long getMaxConcurrentStreams() {
    return DynamicPropertyFactory.getInstance()
        .getLongProperty("servicecomb.rest.server.http2.concurrentStreams",
            HttpServerOptions.DEFAULT_INITIAL_SETTINGS_MAX_CONCURRENT_STREAMS).get();
  }

  public static long getHttp2HeaderTableSize() {
    return DynamicPropertyFactory.getInstance()
        .getLongProperty("servicecomb.rest.server.http2.HeaderTableSize",
            Http2Settings.DEFAULT_HEADER_TABLE_SIZE).get();
  }

  public static boolean getPushEnabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.server.http2.pushEnabled",
            Http2Settings.DEFAULT_ENABLE_PUSH).get();
  }

  public static int getInitialWindowSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.http2.initialWindowSize",
            Http2Settings.DEFAULT_INITIAL_WINDOW_SIZE).get();
  }

  public static int getMaxFrameSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.http2.maxFrameSize",
            Http2Settings.DEFAULT_MAX_FRAME_SIZE).get();
  }

  public static int getMaxHeaderListSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.http2.maxHeaderListSize",
            Http2Settings.DEFAULT_MAX_HEADER_LIST_SIZE).get();
  }

  public static boolean getUseAlpn() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty("servicecomb.rest.server.http2.useAlpnEnabled", true)
        .get();
  }

  public static int getMaxHeaderSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.maxHeaderSize", DEFAULT_SERVER_MAX_HEADER_SIZE)
        .get();
  }

  public static boolean isCorsEnabled() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(SERVICECOMB_CORS_CONFIG_BASE + ".enabled", false)
        .get();
  }

  public static String getCorsAllowedOrigin() {
    return DynamicPropertyFactory.getInstance()
        .getStringProperty(SERVICECOMB_CORS_CONFIG_BASE + ".origin", "*")
        .get();
  }

  public static boolean isCorsAllowCredentials() {
    return DynamicPropertyFactory.getInstance()
        .getBooleanProperty(SERVICECOMB_CORS_CONFIG_BASE + ".allowCredentials", false)
        .get();
  }

  public static Set<String> getCorsAllowedHeaders() {
    String allowedHeaders = DynamicPropertyFactory.getInstance()
        .getStringProperty(SERVICECOMB_CORS_CONFIG_BASE + ".allowedHeader", null)
        .get();
    return convertToSet(allowedHeaders);
  }

  public static Set<String> getCorsAllowedMethods() {
    String allowedMethods = DynamicPropertyFactory.getInstance()
        .getStringProperty(SERVICECOMB_CORS_CONFIG_BASE + ".allowedMethod", null)
        .get();
    return convertToSet(allowedMethods);
  }

  public static Set<String> getCorsExposedHeaders() {
    String exposedHeaders = DynamicPropertyFactory.getInstance()
        .getStringProperty(SERVICECOMB_CORS_CONFIG_BASE + ".exposedHeader", null)
        .get();
    return convertToSet(exposedHeaders);
  }

  public static int getCorsMaxAge() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty(SERVICECOMB_CORS_CONFIG_BASE + ".maxAge", -1)
        .get();
  }

  private static Set<String> convertToSet(String setString) {
    Set<String> resultSet = new HashSet<>();
    if (!StringUtils.isEmpty(setString)) {
      String[] arrString = setString.split(",");
      Stream.of(arrString).map(String::trim).filter(str -> !StringUtils.isEmpty(str))
          .forEach(resultSet::add);
    }
    return resultSet;
  }

  public static int getMaxInitialLineLength() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty("servicecomb.rest.server.maxInitialLineLength",
            HttpServerOptions.DEFAULT_MAX_INITIAL_LINE_LENGTH)
        .get();
  }
}
