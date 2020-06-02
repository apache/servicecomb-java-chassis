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
package org.apache.servicecomb.core.definition;

import static org.apache.servicecomb.core.definition.CoreMetaUtils.CORE_MICROSERVICE_META;
import static org.apache.servicecomb.core.definition.CoreMetaUtils.CORE_MICROSERVICE_VERSION;
import static org.apache.servicecomb.core.definition.CoreMetaUtils.CORE_MICROSERVICE_VERSIONS_META;
import static org.apache.servicecomb.core.definition.CoreMetaUtils.getMicroserviceVersionsMeta;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.event.EnableExceptionPropagation;
import org.apache.servicecomb.foundation.common.event.SubscriberOrder;
import org.apache.servicecomb.registry.api.event.CreateMicroserviceEvent;
import org.apache.servicecomb.registry.api.event.CreateMicroserviceVersionEvent;
import org.apache.servicecomb.registry.api.event.DestroyMicroserviceEvent;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.registry.consumer.MicroserviceVersion;
import org.apache.servicecomb.registry.consumer.MicroserviceVersions;
import org.apache.servicecomb.registry.definition.DefinitionConst;

import com.google.common.eventbus.Subscribe;

import io.swagger.models.Swagger;

/**
 * subscribe event from ServiceRegistry module to create or destroy metas
 */
public class ServiceRegistryListener {
  private final SCBEngine scbEngine;

  public ServiceRegistryListener(SCBEngine scbEngine) {
    this.scbEngine = scbEngine;
    scbEngine.getEventBus().register(this);
  }

  @EnableExceptionPropagation
  @SubscriberOrder(-1000)
  @Subscribe
  public void onCreateMicroservice(CreateMicroserviceEvent event) {
    MicroserviceVersions microserviceVersions = event.getMicroserviceVersions();
    microserviceVersions.getVendorExtensions()
        .put(CORE_MICROSERVICE_VERSIONS_META,
            new ConsumerMicroserviceVersionsMeta(scbEngine, microserviceVersions));
  }

  @EnableExceptionPropagation
  @SubscriberOrder(-1000)
  @Subscribe
  public void onDestroyMicroservice(DestroyMicroserviceEvent event) {

  }

  @EnableExceptionPropagation
  @SubscriberOrder(-1000)
  @Subscribe
  public void onCreateMicroserviceVersion(CreateMicroserviceVersionEvent event) {
    // TODO:如果失败，应该标记出错，以便删除MicroserviceVersions
    MicroserviceVersion microserviceVersion = event.getMicroserviceVersion();
    Microservice microservice = microserviceVersion.getMicroservice();

    // not shortName, to support cross app invoke
    String microserviceName = microserviceVersion.getMicroserviceName();

    MicroserviceMeta microserviceMeta = new MicroserviceMeta(scbEngine, microserviceName, true);
    microserviceMeta.setHandlerChain(scbEngine.getConsumerHandlerManager().getOrCreate(microserviceName));
    MicroserviceVersions microserviceVersions = microserviceVersion.getMicroserviceVersions();
    microserviceMeta.setMicroserviceVersionsMeta(getMicroserviceVersionsMeta(microserviceVersions));

    boolean isServiceCenter = DefinitionConst.REGISTRY_APP_ID.equals(microservice.getAppId())
        && DefinitionConst.REGISTRY_SERVICE_NAME.equals(microservice.getServiceName());
    // do not load service center schemas, because service center did not provide swagger,but can get schema ids....
    // service center better to resolve the problem.
    if (!isServiceCenter) {
      for (String schemaId : microservice.getSchemas()) {
        Swagger swagger = scbEngine.getSwaggerLoader().loadSwagger(microservice, microserviceVersion.getInstances(),
            schemaId);
        microserviceMeta.registerSchemaMeta(schemaId, swagger);
      }
    }

    microserviceMeta.putExtData(CORE_MICROSERVICE_VERSION, microserviceVersion);
    microserviceVersion.getVendorExtensions().put(CORE_MICROSERVICE_META, microserviceMeta);
  }

  public void destroy() {
    scbEngine.getEventBus().unregister(this);
  }
}
