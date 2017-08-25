/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.core.definition;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.servicecomb.foundation.common.RegisterManager;
import io.servicecomb.serviceregistry.api.registry.Microservice;

/**
 * key为microserviceName(app内部)或者appId:microserviceName(跨app)
 */
@Component
public class MicroserviceMetaManager extends RegisterManager<String, MicroserviceMeta> {
  private static final String MICROSERVICE_SCHEMA_MGR = "microservice meta manager";

  private final Object lock = new Object();

  public MicroserviceMetaManager() {
    super(MICROSERVICE_SCHEMA_MGR);
  }

  public SchemaMeta ensureFindSchemaMeta(String microserviceName, String schemaId) {
    MicroserviceMeta microserviceMeta = ensureFindValue(microserviceName);
    return microserviceMeta.ensureFindSchemaMeta(schemaId);
  }

  public Collection<SchemaMeta> getAllSchemaMeta(String microserviceName) {
    MicroserviceMeta microserviceMeta = ensureFindValue(microserviceName);
    return microserviceMeta.getSchemaMetas();
  }

  public MicroserviceMeta getOrCreateMicroserviceMeta(Microservice microservice) {
    String microserviceName = microservice.getServiceName();
    MicroserviceMeta microserviceMeta = getOrCreateMicroserviceMeta(microserviceName);
    if (!StringUtils.isEmpty(microservice.getAlias())) {
      if (findValue(microservice.getAlias()) == null) {
        register(microservice.getAlias(), microserviceMeta);
      }
    }

    return microserviceMeta;
  }

  public MicroserviceMeta getOrCreateMicroserviceMeta(String microserviceName) {
    MicroserviceMeta microserviceMeta = findValue(microserviceName);
    if (microserviceMeta == null) {
      synchronized (lock) {
        microserviceMeta = findValue(microserviceName);
        if (microserviceMeta == null) {
          microserviceMeta = new MicroserviceMeta(microserviceName);
          register(microserviceName, microserviceMeta);
        }
      }
    }

    return microserviceMeta;
  }
}
