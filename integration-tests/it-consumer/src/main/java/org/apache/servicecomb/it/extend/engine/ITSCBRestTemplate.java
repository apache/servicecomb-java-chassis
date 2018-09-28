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

import org.apache.servicecomb.core.definition.MicroserviceVersionMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.apache.servicecomb.provider.springmvc.reference.CseRestTemplate;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.consumer.MicroserviceVersionRule;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;

public class ITSCBRestTemplate extends CseRestTemplate {
  private String urlPrefix;

  private String schemaId;

  public ITSCBRestTemplate(String schemaId) {
    this.schemaId = schemaId;
  }

  public ITSCBRestTemplate init() {
    String producerName = ITJUnitUtils.getProducerName();
    MicroserviceVersionRule microserviceVersionRule = RegistryUtils.getServiceRegistry().getAppManager()
        .getOrCreateMicroserviceVersionRule(RegistryUtils.getAppId(), producerName,
            DefinitionConst.VERSION_RULE_ALL);
    MicroserviceVersionMeta microserviceVersionMeta = microserviceVersionRule.getLatestMicroserviceVersion();
    SchemaMeta schemaMeta = microserviceVersionMeta.getMicroserviceMeta().ensureFindSchemaMeta(schemaId);
    urlPrefix = String.format("cse://%s/%s", producerName, schemaMeta.getSwagger().getBasePath());

    setUriTemplateHandler(new ITUriTemplateHandler(urlPrefix));
    setRequestFactory(new ITClientHttpRequestFactory());

    return this;
  }

  public String getUrlPrefix() {
    return urlPrefix;
  }
}
