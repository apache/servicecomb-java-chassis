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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.foundation.common.VendorExtensions;
import org.apache.servicecomb.serviceregistry.definition.MicroserviceNameParser;
import org.apache.servicecomb.swagger.SwaggerUtils;

import io.swagger.models.Swagger;

/**
 * should named MicroserviceVersionMeta<br>
 * but for compatible reason, keep the old name
 */
public class MicroserviceMeta {
  private final SCBEngine scbEngine;

  private MicroserviceVersionsMeta microserviceVersionsMeta;

  private String appId;

  // always not include appId
  private String shortName;

  // inside app: equals to shortName
  // cross app: appId:shortName
  private String microserviceName;

  // key is schemaId, this is all schemas
  private Map<String, SchemaMeta> schemaMetas = new HashMap<>();

  // key is schema interface
  // only when list have only one element, then allow query by interface
  // otherwise must query by schemaId
  //
  // value is synchronizedList, only for low frequency query
  private Map<Class<?>, List<SchemaMeta>> intfSchemaMetas = new HashMap<>();

  // key is OperationMeta.getMicroserviceQualifiedName()
  private Map<String, OperationMeta> operationMetas = new HashMap<>();

  private boolean consumer = true;

  private List<Handler> consumerHandlerChain;

  private List<Handler> providerHandlerChain;

  // providerQpsFlowControlHandler is a temporary field, only for internal usage
  private Handler providerQpsFlowControlHandler;

  // providerQpsFlowControlHandlerSearched is a temporary field, only for internal usage
  private boolean providerQpsFlowControlHandlerSearched;

  private VendorExtensions vendorExtensions = new VendorExtensions();

  public MicroserviceMeta(SCBEngine scbEngine, String microserviceName, List<Handler> consumerHandlerChain,
      List<Handler> providerHandlerChain, boolean consumer) {
    this.scbEngine = scbEngine;
    MicroserviceNameParser parser = scbEngine.parseMicroserviceName(microserviceName);
    this.appId = parser.getAppId();
    this.shortName = parser.getShortName();
    this.microserviceName = parser.getMicroserviceName();

    this.consumerHandlerChain = consumerHandlerChain;
    this.providerHandlerChain = providerHandlerChain;
	this.consumer = consumer;
  }

  public MicroserviceConfig getMicroserviceConfig() {
    return microserviceVersionsMeta.getMicroserviceConfig();
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

  public String getShortName() {
    return shortName;
  }

  public SchemaMeta registerSchemaMeta(String schemaId, Swagger swagger) {
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

    schemaMeta.getOperations().values().stream()
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

  public void putExtData(String key, Object data) {
    vendorExtensions.put(key, data);
  }

  public <T> T getExtData(String key) {
    return vendorExtensions.get(key);
  }

  public List<Handler> getConsumerHandlerChain() {
    return consumerHandlerChain;
  }

  public List<Handler> getProviderHandlerChain() {
    return providerHandlerChain;
  }

  /**
   * Only for JavaChassis internal usage.
   */
  @Deprecated
  public Handler getProviderQpsFlowControlHandler() {
    if (providerQpsFlowControlHandlerSearched) {
      return providerQpsFlowControlHandler;
    }

    List<Handler> providerHandlerChain = getProviderHandlerChain();
    for (Handler handler : providerHandlerChain) {
      // matching by class name is more or less better than importing an extra maven dependency
      if ("org.apache.servicecomb.qps.ProviderQpsFlowControlHandler".equals(handler.getClass().getName())) {
        providerQpsFlowControlHandler = handler;
        break;
      }
    }
    providerQpsFlowControlHandlerSearched = true;
    return providerQpsFlowControlHandler;
  }
}
