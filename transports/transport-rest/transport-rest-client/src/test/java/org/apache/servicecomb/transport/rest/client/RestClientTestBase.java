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
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.foundation.vertx.client.http.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.vertx.core.http.HttpClientRequest;

public class RestClientTestBase {
  static SCBEngine scbEngine;

  static Transport restTransport = new FakeRestTransport();

  static RestClientTransportContextFactory factory = new RestClientTransportContextFactory()
      .setBoundaryFactory(() -> "my-boundary");

  ReferenceConfig referenceConfig = new ReferenceConfig(Const.RESTFUL, Const.DEFAULT_VERSION_RULE);

  OperationMeta operationMeta;

  RestOperationMeta restOperationMeta;

  Invocation invocation;

  RestClientTransportContext transportContext;

  HttpClientRequest httpClientRequest;

  @BeforeAll
  static void beforeAll() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta(SCHEMA_ID, new RestFeatureController())
        .run();
    HttpClients.load();
  }

  @AfterAll
  static void afterAll() {
    scbEngine.destroy();
    HttpClients.destroy();

    ArchaiusUtils.resetConfig();
  }

  void init(String operationId, Map<String, Object> swaggerArgs, boolean ssl) {
    operationMeta = scbEngine.getProducerMicroserviceMeta()
        .ensureFindSchemaMeta(SCHEMA_ID)
        .ensureFindOperation(operationId);
    restOperationMeta = RestMetaUtils.getRestOperationMeta(operationMeta);

    invocation = InvocationFactory.forConsumer(
        referenceConfig, operationMeta, new InvocationRuntimeType(null), swaggerArgs);

    String url = "rest://localhost:1234?sslEnabled=" + ssl;
    invocation.setEndpoint(new Endpoint(restTransport, url));

    transportContext = factory.create(invocation);
    invocation.setTransportContext(transportContext);
    httpClientRequest = transportContext.getHttpClientRequest();
  }

  String absoluteURI() {
    return httpClientRequest.absoluteURI();
  }
}
