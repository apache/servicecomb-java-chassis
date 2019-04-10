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
package org.apache.servicecomb.serviceregistry.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.swagger.SwaggerUtils;

import io.swagger.models.Swagger;

public class MicroserviceMeta {
  private String appId;

  // always not include appId
  private String shortName;

  // inside app: equals to shortName
  // cross app: appId:shortName
  private String microserviceName;

  // key is schemaId, this is all schemas
  private Map<String, SchemaMeta> schemaMetas = new ConcurrentHashMapEx<>();

  // key is schema interface
  // only when list have only one element, then allow query by interface
  // otherwise must query by schemaId
  //
  // value is synchronizedList, only for low frequency query
  private Map<Class<?>, List<SchemaMeta>> intfSchemaMetas = new ConcurrentHashMapEx<>();

  private Map<String, Object> vendorExtensions = new ConcurrentHashMapEx<>();

  private boolean consumer = true;

  public MicroserviceMeta(String microserviceName) {
    MicroserviceNameParser parser = new MicroserviceNameParser(microserviceName);
    this.appId = parser.getAppId();
    this.shortName = parser.getShortName();
    this.microserviceName = parser.getMicroserviceName();
  }

  public boolean isConsumer() {
    return consumer;
  }

  public void setConsumer(boolean consumer) {
    this.consumer = consumer;
  }

  public MicroserviceMeta consumer(boolean consumer) {
    this.consumer = consumer;
    return this;
  }

  public String getMicroserviceName() {
    return microserviceName;
  }

  public String getAppId() {
    return appId;
  }

  public String getShortName() {
    return shortName;
  }

  public void registerSchemaMeta(String schemaId, Swagger swagger) {
    SchemaMeta schemaMeta = new SchemaMeta(this, schemaId, swagger);

    if (schemaMetas.putIfAbsent(schemaMeta.getSchemaId(), schemaMeta) != null) {
      throw new IllegalStateException(String.format(
          "failed to register SchemaMeta caused by duplicated schemaId, appId=%s, microserviceName=%s, schemaId=%s.",
          appId, microserviceName, schemaMeta.getSchemaId()));
    }

    Class<?> intf = SwaggerUtils.getInterface(schemaMeta.getSwagger());
    if (intf != null) {
      intfSchemaMetas
          .computeIfAbsent(intf, k -> Collections.synchronizedList(new ArrayList<>()))
          .add(schemaMeta);
    }
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

  public SchemaMeta findSchemaMeta(Class<?> schemaIntf) {
    List<SchemaMeta> schemaList = intfSchemaMetas.get(schemaIntf);
    if (schemaList == null) {
      return null;
    }

    if (schemaList.size() > 1) {
      throw new IllegalStateException(String.format(
          "failed to find SchemaMeta by interface cause there are multiple SchemaMeta relate to the interface, "
              + "please use schemaId to choose a SchemaMeta, "
              + "appId=%s, microserviceName=%s, interface=%s.",
          appId, microserviceName, schemaIntf.getName()));
    }

    return schemaList.get(0);
  }

  public Map<String, SchemaMeta> getSchemaMetas() {
    return schemaMetas;
  }

  public Map<String, Object> getVendorExtensions() {
    return vendorExtensions;
  }
}
