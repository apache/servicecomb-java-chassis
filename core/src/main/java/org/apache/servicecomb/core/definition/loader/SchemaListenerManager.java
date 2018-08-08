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

package org.apache.servicecomb.core.definition.loader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * key为microserviceName
 */
@Component
public class SchemaListenerManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaListenerManager.class);

  @Autowired(required = false)
  private List<SchemaListener> schemaListenerList = new ArrayList<>();

  public void setSchemaListenerList(List<SchemaListener> schemaListenerList) {
    this.schemaListenerList = schemaListenerList;
  }

  public void notifySchemaListener(MicroserviceMeta... microserviceMetas) {
    List<SchemaMeta> schemaMetaList = new ArrayList<>();
    for (MicroserviceMeta microserviceMeta : microserviceMetas) {
      schemaMetaList.addAll(microserviceMeta.getSchemaMetas());
    }
    notifySchemaListener(schemaMetaList.toArray(new SchemaMeta[schemaMetaList.size()]));
  }

  public void notifySchemaListener() {
    //only one instance
    MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroMeta();
    notifySchemaListener(microserviceMeta);
  }

  public void notifySchemaListener(SchemaMeta... schemaMetas) {
    for (SchemaListener listener : schemaListenerList) {
      listener.onSchemaLoaded(schemaMetas);
    }
  }

  public void notifySchemaListener(List<SchemaMeta> schemaMetaList) {
    SchemaMeta[] schemaMetas = schemaMetaList.toArray(new SchemaMeta[schemaMetaList.size()]);
    notifySchemaListener(schemaMetas);
  }

  public SchemaMeta ensureFindSchemaMeta(String microserviceName, String schemaId) {
    if (!RegistryUtils.getMicroservice().getServiceName().equals(microserviceName)) {
      LOGGER.error("miroserviceName : {} is different from the default microserviceName :{}",
          microserviceName,
          RegistryUtils.getMicroservice().getServiceName());
    }
    MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroMeta();
    return microserviceMeta.ensureFindSchemaMeta(schemaId);
  }

  public Collection<SchemaMeta> getAllSchemaMeta(String microserviceName) {
    if (!RegistryUtils.getMicroservice().getServiceName().equals(microserviceName)) {
      LOGGER.error("miroserviceName : {} is different from the default microserviceName :{}",
          microserviceName,
          RegistryUtils.getMicroservice().getServiceName());
    }
    MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroMeta();
    return microserviceMeta.getSchemaMetas();
  }
}
