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
package org.apache.servicecomb.it.extend.engine;

import java.util.Optional;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.provider.consumer.MicroserviceReferenceConfig;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;

public class ITSCBRestTemplate extends CseRestTemplate {
  private String urlPrefix;

  private String schemaId;

  private String basePath;

  private MicroserviceInstance instance;

  public ITSCBRestTemplate(String schemaId) {
    this.schemaId = schemaId;
  }

  public ITSCBRestTemplate init() {
    String producerName = ITJUnitUtils.getProducerName();
    MicroserviceReferenceConfig microserviceReferenceConfig = SCBEngine.getInstance()
        .createMicroserviceReferenceConfig(producerName);
    MicroserviceMeta microserviceMeta = microserviceReferenceConfig.getLatestMicroserviceMeta();
    SchemaMeta schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);
    basePath = schemaMeta.getSwagger().getBasePath();
    urlPrefix = String.format("cse://%s%s", producerName, basePath);
    instance = DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceManager(RegistrationManager.INSTANCE.getMicroservice().getAppId())
        .getOrCreateMicroserviceVersions(producerName).getPulledInstances().get(0);

    setUriTemplateHandler(new ITUriTemplateHandler(urlPrefix));
    setRequestFactory(new ITClientHttpRequestFactory());

    return this;
  }

  public String getBasePath() {
    return basePath;
  }

  public String getUrlPrefix() {
    return urlPrefix;
  }

  public String getAddress(String transport) {
    Optional<String> addressHolder = instance.getEndpoints().stream()
        .filter(endpoint -> endpoint.startsWith(transport))
        .findFirst();
    return addressHolder.get();
  }
}
