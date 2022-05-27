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

import static org.apache.servicecomb.core.definition.CoreMetaUtils.CORE_MICROSERVICE_META;

import java.util.Comparator;
import java.util.Optional;

import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.apache.servicecomb.provider.springmvc.reference.async.CseAsyncRestTemplate;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.registry.consumer.MicroserviceManager;
import org.apache.servicecomb.registry.consumer.MicroserviceVersion;
import org.apache.servicecomb.registry.consumer.MicroserviceVersions;

public class ITSCBAsyncRestTemplate extends CseAsyncRestTemplate {
  private String urlPrefix;

  private String schemaId;

  private String basePath;

  private MicroserviceInstance instance;

  public ITSCBAsyncRestTemplate(String schemaId) {
    this.schemaId = schemaId;
  }

  public ITSCBAsyncRestTemplate init() {
    String producerName = ITJUnitUtils.getProducerName();

    ensureProviderBasePath(producerName);

    urlPrefix = String.format("cse://%s%s", producerName, basePath);
    instance = DiscoveryManager.INSTANCE.getAppManager()
        .getOrCreateMicroserviceManager(RegistrationManager.INSTANCE.getMicroservice().getAppId())
        .getOrCreateMicroserviceVersions(producerName).getPulledInstances().get(0);

    setUriTemplateHandler(new ITUriTemplateHandler(urlPrefix));
    setAsyncRequestFactory(new ITAsyncClientHttpRequestFactory());

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

  private void ensureProviderBasePath(String producerName) {
    MicroserviceManager microserviceManager =
        DiscoveryManager.INSTANCE.getAppManager()
            .getOrCreateMicroserviceManager(RegistrationManager.INSTANCE.getMicroservice().getAppId());
    MicroserviceVersions producerMicroserviceVersions =
        microserviceManager.getOrCreateMicroserviceVersions(producerName);
    Optional<MicroserviceVersion> latestMicroserviceVersion =
        producerMicroserviceVersions.getVersions().values().stream()
            .max(Comparator.comparing(MicroserviceVersion::getVersion));
    latestMicroserviceVersion.ifPresent(microserviceVersion -> {
      MicroserviceMeta microserviceMeta = microserviceVersion.getVendorExtensions().get(CORE_MICROSERVICE_META);
      SchemaMeta schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);
      basePath = schemaMeta.getSwagger().getBasePath();
    });
    latestMicroserviceVersion.orElseThrow(() -> new IllegalStateException("cannot find producer: " + producerName));
  }
}
