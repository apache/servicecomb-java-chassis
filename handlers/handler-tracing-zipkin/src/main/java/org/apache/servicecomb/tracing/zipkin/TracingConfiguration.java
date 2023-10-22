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

package org.apache.servicecomb.tracing.zipkin;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_ADDRESS;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_API_V1;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_API_V2;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_API_VERSION;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_TRACING_COLLECTOR_PATH;
import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.DEFAULT_TRACING_COLLECTOR_ADDRESS;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.DynamicProperties;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import zipkin2.Span;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

@Configuration
class TracingConfiguration {
  private String apiVersion = CONFIG_TRACING_COLLECTOR_API_V2;

  public static final String TRACING_WORK_WITH_THIRDPARTY = "servicecomb.tracing.workWithThirdParty";

  @Bean
  Sender sender(DynamicProperties dynamicProperties) {
    apiVersion = dynamicProperties.getStringProperty(CONFIG_TRACING_COLLECTOR_API_VERSION,
        CONFIG_TRACING_COLLECTOR_API_V2).toLowerCase();
    // use default value if the user set value is invalid
    if (apiVersion.compareTo(CONFIG_TRACING_COLLECTOR_API_V1) != 0) {
      apiVersion = CONFIG_TRACING_COLLECTOR_API_V2;
    }

    String path = MessageFormat.format(CONFIG_TRACING_COLLECTOR_PATH, apiVersion);
    return OkHttpSender.create(
        dynamicProperties.getStringProperty(
                CONFIG_TRACING_COLLECTOR_ADDRESS,
                DEFAULT_TRACING_COLLECTOR_ADDRESS)
            .trim()
            .replaceAll("/+$", "")
            .concat(path));
  }

  @Bean
  Reporter<Span> zipkinReporter(Sender sender) {
    if (apiVersion.compareTo(CONFIG_TRACING_COLLECTOR_API_V1) == 0) {
      return AsyncReporter.builder(sender).build(SpanBytesEncoder.JSON_V1);
    }

    return AsyncReporter.builder(sender).build();
  }


  @Bean
  Tracing tracing(Sender sender, DynamicProperties dynamicProperties,
      CurrentTraceContext currentTraceContext) {
    return Tracing.newBuilder()
        .localServiceName(BootStrapProperties.readServiceName())
        .currentTraceContext(currentTraceContext) // puts trace IDs into logs
        .addSpanHandler(AsyncZipkinSpanHandler.create(sender))
        .build();
  }

  @Bean
  CurrentTraceContext currentTraceContext() {
    return ThreadLocalCurrentTraceContext.newBuilder().addScopeDecorator(MDCScopeDecorator.newBuilder().build())
        .build();
  }

  @Bean
  HttpTracing httpTracing(Tracing tracing) {
    return HttpTracing.create(tracing);
  }

  public static String createRequestPath(Invocation invocation) throws Exception {
    URIEndpointObject address = (URIEndpointObject) invocation.getEndpoint().getAddress();
    String urlPrefix = address.getFirst(DefinitionConst.URL_PREFIX);
    RestOperationMeta swaggerRestOperation = invocation.getOperationMeta().getExtData(RestConst.SWAGGER_REST_OPERATION);
    String path = (String) invocation.getHandlerContext().get(RestConst.REST_CLIENT_REQUEST_PATH);
    if (path == null) {
      path = swaggerRestOperation.getPathBuilder().createRequestPath(invocation.getSwaggerArguments());
    }

    if (StringUtils.isEmpty(urlPrefix) || path.startsWith(urlPrefix)) {
      return path;
    }

    return urlPrefix + path;
  }
}
