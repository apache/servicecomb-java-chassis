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
import org.apache.servicecomb.config.LegacyPropertyFactory;
import org.apache.servicecomb.transport.common.TransportConfigUtils;

import io.vertx.core.Verticle;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpServerOptions;

public final class TransportConfig {
  public static final int DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND = 180;

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
    return LegacyPropertyFactory.getStringProperty("servicecomb.rest.address", null);
  }

  public static int getMaxFormAttributeSize() {
    return LegacyPropertyFactory.getIntProperty("servicecomb.rest.server.maxFormAttributeSize",
        HttpServerOptions.DEFAULT_MAX_FORM_ATTRIBUTE_SIZE);
  }

  public static int getCompressionLevel() {
    return LegacyPropertyFactory.getIntProperty("servicecomb.rest.server.compressionLevel",
        HttpServerOptions.DEFAULT_COMPRESSION_LEVEL);
  }

  public static int getMaxChunkSize() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.maxChunkSize",
            HttpServerOptions.DEFAULT_MAX_CHUNK_SIZE);
  }

  public static int getDecoderInitialBufferSize() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.decoderInitialBufferSize",
            HttpServerOptions.DEFAULT_DECODER_INITIAL_BUFFER_SIZE);
  }

  public static int getHttp2ConnectionWindowSize() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.http2ConnectionWindowSize",
            HttpServerOptions.DEFAULT_HTTP2_CONNECTION_WINDOW_SIZE);
  }

  public static int getThreadCount() {
    return TransportConfigUtils.readVerticleCount(
        "servicecomb.rest.server.verticle-count",
        "servicecomb.rest.server.thread-count");
  }

  public static int getConnectionIdleTimeoutInSeconds() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.connection.idleTimeoutInSeconds",
            DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND)
        ;
  }

  public static int getHttp2ConnectionIdleTimeoutInSeconds() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.http2.connection.idleTimeoutInSeconds",
            DEFAULT_SERVER_CONNECTION_IDLE_TIMEOUT_SECOND);
  }

  public static boolean getCompressed() {
    return LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.server.compression", DEFAULT_SERVER_COMPRESSION_SUPPORT);
  }

  public static boolean getDecompressionSupported() {
    return LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.server.decompressionSupported",
            HttpServerOptions.DEFAULT_DECOMPRESSION_SUPPORTED);
  }

  public static long getMaxConcurrentStreams() {
    return LegacyPropertyFactory
        .getLongProperty("servicecomb.rest.server.http2.concurrentStreams",
            HttpServerOptions.DEFAULT_INITIAL_SETTINGS_MAX_CONCURRENT_STREAMS);
  }

  public static long getHttp2HeaderTableSize() {
    return LegacyPropertyFactory
        .getLongProperty("servicecomb.rest.server.http2.HeaderTableSize",
            Http2Settings.DEFAULT_HEADER_TABLE_SIZE);
  }

  public static boolean getPushEnabled() {
    return LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.server.http2.pushEnabled",
            Http2Settings.DEFAULT_ENABLE_PUSH);
  }

  public static int getInitialWindowSize() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.http2.initialWindowSize",
            Http2Settings.DEFAULT_INITIAL_WINDOW_SIZE);
  }

  public static int getMaxFrameSize() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.http2.maxFrameSize",
            Http2Settings.DEFAULT_MAX_FRAME_SIZE);
  }

  public static int getMaxHeaderListSize() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.http2.maxHeaderListSize",
            Http2Settings.DEFAULT_MAX_HEADER_LIST_SIZE);
  }

  public static boolean getUseAlpn() {
    return LegacyPropertyFactory
        .getBooleanProperty("servicecomb.rest.server.http2.useAlpnEnabled", true);
  }

  public static int getMaxHeaderSize() {
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.maxHeaderSize", DEFAULT_SERVER_MAX_HEADER_SIZE);
  }

  public static boolean isCorsEnabled() {
    return LegacyPropertyFactory
        .getBooleanProperty(SERVICECOMB_CORS_CONFIG_BASE + ".enabled", false);
  }

  public static String getCorsAllowedOrigin() {
    return LegacyPropertyFactory
        .getStringProperty(SERVICECOMB_CORS_CONFIG_BASE + ".origin", "*");
  }

  public static boolean isCorsAllowCredentials() {
    return LegacyPropertyFactory
        .getBooleanProperty(SERVICECOMB_CORS_CONFIG_BASE + ".allowCredentials", false);
  }

  public static Set<String> getCorsAllowedHeaders() {
    String allowedHeaders = LegacyPropertyFactory
        .getStringProperty(SERVICECOMB_CORS_CONFIG_BASE + ".allowedHeader");
    return convertToSet(allowedHeaders);
  }

  public static Set<String> getCorsAllowedMethods() {
    String allowedMethods = LegacyPropertyFactory
        .getStringProperty(SERVICECOMB_CORS_CONFIG_BASE + ".allowedMethod");
    return convertToSet(allowedMethods);
  }

  public static Set<String> getCorsExposedHeaders() {
    String exposedHeaders = LegacyPropertyFactory
        .getStringProperty(SERVICECOMB_CORS_CONFIG_BASE + ".exposedHeader");
    return convertToSet(exposedHeaders);
  }

  public static int getCorsMaxAge() {
    return LegacyPropertyFactory
        .getIntProperty(SERVICECOMB_CORS_CONFIG_BASE + ".maxAge", -1);
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
    return LegacyPropertyFactory
        .getIntProperty("servicecomb.rest.server.maxInitialLineLength",
            HttpServerOptions.DEFAULT_MAX_INITIAL_LINE_LENGTH);
  }
}
