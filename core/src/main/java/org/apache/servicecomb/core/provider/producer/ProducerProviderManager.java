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

package org.apache.servicecomb.core.provider.producer;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.servicecomb.core.ProducerProvider;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.MicroserviceMetaManager;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.SchemaUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProducerProviderManager {
  @Autowired(required = false)
  private List<ProducerProvider> producerProviderList = Collections.emptyList();

  @Inject
  private MicroserviceMetaManager microserviceMetaManager;

  public void init() throws Exception {
    for (ProducerProvider provider : producerProviderList) {
      provider.init();
    }

    Microservice microservice = RegistryUtils.getMicroservice();
    MicroserviceMeta microserviceMeta = microserviceMetaManager.getOrCreateMicroserviceMeta(microservice);
    for (SchemaMeta schemaMeta : microserviceMeta.getSchemaMetas()) {
      String content = SchemaUtils.swaggerToString(schemaMeta.getSwagger());
      microservice.addSchema(schemaMeta.getSchemaId(), content);
    }
  }
}
