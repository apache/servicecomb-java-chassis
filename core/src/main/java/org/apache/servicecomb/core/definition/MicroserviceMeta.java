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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.filter.FilterNode;
import org.apache.servicecomb.foundation.common.VendorExtensions;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * should named MicroserviceVersionMeta<br>
 * but for compatible reason, keep the old name
 */
public class MicroserviceMeta {
  private final SCBEngine scbEngine;

  private MicroserviceVersionsMeta microserviceVersionsMeta;

  private final String appId;

  private final String microserviceName;

  // key is schemaId, this is all schemas
  private final Map<String, SchemaMeta> schemaMetas = new HashMap<>();

  // key is OperationMeta.getMicroserviceQualifiedName()
  private final Map<String, OperationMeta> operationMetas = new HashMap<>();

  private final boolean consumer;

  private FilterNode filterChain = FilterNode.EMPTY;

  private final VendorExtensions vendorExtensions = new VendorExtensions();

  public MicroserviceMeta(SCBEngine scbEngine, String application, String serviceName, boolean consumer) {
    this.scbEngine = scbEngine;
    this.appId = application;
    this.microserviceName = serviceName;
    this.consumer = consumer;
  }

  public MicroserviceVersionsMeta getMicroserviceVersionsMeta() {
    return microserviceVersionsMeta;
  }

  public void setMicroserviceVersionsMeta(MicroserviceVersionsMeta microserviceVersionsMeta) {
    this.microserviceVersionsMeta = microserviceVersionsMeta;
  }

  public SCBEngine getScbEngine() {
    return scbEngine;
  }

  public boolean isConsumer() {
    return consumer;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public String getAppId() {
    return appId;
  }

  public SchemaMeta registerSchemaMeta(String schemaId, OpenAPI swagger) {
    SchemaMeta schemaMeta = new SchemaMeta(this, schemaId, swagger);

    if (schemaMetas.putIfAbsent(schemaMeta.getSchemaId(), schemaMeta) != null) {
      throw new IllegalStateException(String.format(
          "failed to register SchemaMeta caused by duplicated schemaId, appId=%s, microserviceName=%s, schemaId=%s.",
          appId, microserviceName, schemaMeta.getSchemaId()));
    }

    schemaMeta.getOperations().values()
        .forEach(operationMeta -> operationMetas.put(operationMeta.getMicroserviceQualifiedName(), operationMeta));

    return schemaMeta;
  }

  public Map<String, OperationMeta> operationMetas() {
    return operationMetas;
  }

  public Collection<OperationMeta> getOperations() {
    return operationMetas.values();
  }

  public SchemaMeta ensureFindSchemaMeta(String schemaId) {
    SchemaMeta schemaMeta = schemaMetas.get(schemaId);
    if (schemaMeta == null) {
      throw new IllegalStateException(String.format(
          "failed to find SchemaMeta by schemaId, appId=%s, microserviceName=%s, schemaId=%s.",
          appId, microserviceName, schemaId));
    }

    return schemaMeta;
  }

  public SchemaMeta findSchemaMeta(String schemaId) {
    return schemaMetas.get(schemaId);
  }

  public Map<String, SchemaMeta> getSchemaMetas() {
    return schemaMetas;
  }

  public void putExtData(String key, Object data) {
    vendorExtensions.put(key, data);
  }

  public <T> T getExtData(String key) {
    return vendorExtensions.get(key);
  }

  public FilterNode getFilterChain() {
    return filterChain;
  }

  public void setFilterChain(FilterNode filterChain) {
    this.filterChain = filterChain;
  }
}
