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

package org.apache.servicecomb.transport.rest.client;

import static org.apache.servicecomb.transport.rest.client.RestFeatureController.SCHEMA_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;

import org.apache.servicecomb.common.rest.definition.RestMetaUtils;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.InvocationRuntimeType;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.invocation.InvocationFactory;
import org.apache.servicecomb.core.provider.consumer.ReferenceConfig;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

class RestClientTransportContextFactoryTest {
  static SCBEngine scbEngine;

  static Transport restTransport = new AbstractTransport() {
    @Override
    public String getName() {
      return null;
    }

    @Override
    public boolean init() {
      return false;
    }

    @Override
    public void send(Invocation invocation, AsyncResponse asyncResp) {

    }

    @Override
    public Object parseAddress(String address) {
      return new URIEndpointObject(address);
    }
  };

  static RestClientTransportContextFactory factory = new RestClientTransportContextFactory()
      .setBoundaryFactory(BoundaryFactory.DEFAULT);

  static OperationMeta operationMeta;

  static RestOperationMeta restOperationMeta;

  static ReferenceConfig referenceConfig = new ReferenceConfig(Const.RESTFUL, Const.DEFAULT_VERSION_RULE);

  @BeforeAll
  static void beforeAll() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta(SCHEMA_ID, new RestFeatureController())
        .run();
    operationMeta = scbEngine.getProducerMicroserviceMeta()
        .ensureFindSchemaMeta(SCHEMA_ID)
        .ensureFindOperation("query");
    restOperationMeta = RestMetaUtils.getRestOperationMeta(operationMeta);
    HttpClients.load();
  }

  @AfterAll
  static void afterAll() {
    scbEngine.destroy();
    HttpClients.destroy();

    ArchaiusUtils.resetConfig();
  }

  Invocation invocation;

  RestClientTransportContext transportContext;

  void initInvocation(Map<String, Object> swaggerArgs, boolean ssl) {
    invocation = InvocationFactory.forConsumer(
        referenceConfig, operationMeta, new InvocationRuntimeType(null), swaggerArgs);

    String url = "rest://localhost:1234?sslEnabled=" + ssl;
    invocation.setEndpoint(new Endpoint(restTransport, url));
  }

  String absoluteURI() {
    return transportContext.getHttpClientRequest().absoluteURI();
  }

  @Test
  void should_create_without_ssl() {
    initInvocation(null, false);

    transportContext = factory.create(invocation);
    assertThat(absoluteURI()).isEqualTo("http://localhost:1234/query");
  }

  @Test
  void should_create_with_ssl() {
    initInvocation(null, true);

    transportContext = factory.create(invocation);
    assertThat(absoluteURI()).isEqualTo("https://localhost:1234/query");
  }

  @Test
  void should_create_with_query() {
    initInvocation(ImmutableMap.of("query", "value"), true);

    transportContext = factory.create(invocation);
    assertThat(absoluteURI()).isEqualTo("https://localhost:1234/query?query=value");
  }

  @Test
  void should_create_with_query_list() {
    initInvocation(ImmutableMap.of("query", Arrays.asList("v1", "v2")), true);

    transportContext = factory.create(invocation);
    assertThat(absoluteURI()).isEqualTo("https://localhost:1234/query?query=v1&query=v2");
  }

  @Test
  void should_create_with_query_array() {
    initInvocation(ImmutableMap.of("query", new String[] {"v1", "v2"}), true);

    transportContext = factory.create(invocation);
    assertThat(absoluteURI()).isEqualTo("https://localhost:1234/query?query=v1&query=v2");
  }

  @Test
  void should_get_local_address_as_not_connected_before_connect() {
    initInvocation(null, true);

    transportContext = factory.create(invocation);
    assertThat(transportContext.getLocalAddress()).isEqualTo("not connected");
  }
}