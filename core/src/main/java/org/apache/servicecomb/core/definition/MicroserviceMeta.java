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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.foundation.common.RegisterManager;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.api.Const;

/**
 * 微服务名为microserviceName(app内部)或者appId:microserviceName(跨app)
 * operation的查询key为operation的qualifiedName
 */
public class MicroserviceMeta extends CommonService<OperationMeta> {
  private String appId;

  // 不包括appId的名字
  private String shortName;

  // 如果要生成class，在这个loader中创建
  private ClassLoader classLoader;

  // key为schema id
  private RegisterManager<String, SchemaMeta> idSchemaMetaMgr;

  // key为schema interface
  // 只有一个interface对应一个schemaMeta时，才允许根据接口查询schema
  // 否则直接抛异常，只能显式地指定schemaId来使用
  private Map<Class<?>, List<SchemaMeta>> intfSchemaMetaMgr = new ConcurrentHashMap<>();

  private final Object intfSchemaLock = new Object();

  private Map<String, Object> extData = new ConcurrentHashMap<>();

  public MicroserviceMeta(String microserviceName) {
    classLoader = JvmUtils.findClassLoader();
    parseMicroserviceName(microserviceName);
    createOperationMgr("Operation meta mgr for microservice " + microserviceName);
    idSchemaMetaMgr = new RegisterManager<>("Schema meta id mgr for microservice " + microserviceName);
  }

  public void regSchemaMeta(SchemaMeta schemaMeta) {
    idSchemaMetaMgr.register(schemaMeta.getSchemaId(), schemaMeta);
    regSchemaAndInterface(schemaMeta);

    for (OperationMeta operationMeta : schemaMeta.getOperations()) {
      regOperation(operationMeta.getSchemaQualifiedName(), operationMeta);
    }
  }

  private void regSchemaAndInterface(SchemaMeta schemaMeta) {
    Class<?> intf = schemaMeta.getSwaggerIntf();
    synchronized (intfSchemaLock) {
      List<SchemaMeta> schemaList = intfSchemaMetaMgr.computeIfAbsent(intf, k -> new ArrayList<>());

      schemaList.add(schemaMeta);
    }
  }

  public SchemaMeta ensureFindSchemaMeta(String schemaId) {
    return idSchemaMetaMgr.ensureFindValue(schemaId);
  }

  public SchemaMeta findSchemaMeta(String schemaId) {
    return idSchemaMetaMgr.findValue(schemaId);
  }

  public SchemaMeta ensureFindSchemaMeta(Class<?> schemaIntf) {
    SchemaMeta schemaMeta = findSchemaMeta(schemaIntf);
    if (schemaMeta == null) {
      String msg =
          String.format("No schema interface is %s.", schemaIntf.getName());
      throw new Error(msg);
    }

    return schemaMeta;
  }

  public SchemaMeta findSchemaMeta(Class<?> schemaIntf) {
    List<SchemaMeta> schemaList = intfSchemaMetaMgr.get(schemaIntf);
    if (schemaList == null) {
      return null;
    }

    if (schemaList.size() > 1) {
      String msg =
          String.format("More than one schema interface is %s, please use schemaId to choose a schema.",
              schemaIntf.getName());
      throw new Error(msg);
    }

    synchronized (intfSchemaLock) {
      return schemaList.get(0);
    }
  }

  public Collection<SchemaMeta> getSchemaMetas() {
    return idSchemaMetaMgr.values();
  }

  public void putExtData(String key, Object data) {
    extData.put(key, data);
  }

  @SuppressWarnings("unchecked")
  public <T> T getExtData(String key) {
    return (T) extData.get(key);
  }

  public String getAppId() {
    return appId;
  }

  public String getShortName() {
    return shortName;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  protected void parseMicroserviceName(String microserviceName) {
    int idxAt = microserviceName.indexOf(Const.APP_SERVICE_SEPARATOR);
    if (idxAt == -1) {
      appId = RegistryUtils.getAppId();
      name = microserviceName;
      shortName = name;
      return;
    }

    appId = microserviceName.substring(0, idxAt);
    name = microserviceName;
    shortName = microserviceName.substring(idxAt + 1);
  }
}
