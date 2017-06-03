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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.foundation.common.RegisterManager;

/**
 * 微服务名为microserviceName(app内部)或者appId:microserviceName(跨app)
 * operation的查询key为operation的qualifiedName
 *
 * @version  [版本号, 2016年12月29日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
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
    private RegisterManager<Class<?>, SchemaMeta> intfSchemaMetaMgr;

    private Map<String, Object> extData = new ConcurrentHashMap<>();

    public MicroserviceMeta(String microserviceName) {
        classLoader = Thread.currentThread().getContextClassLoader();
        parseMicroserviceName(microserviceName);
        createOperationMgr("Operation meta mgr for microservice " + microserviceName);
        idSchemaMetaMgr = new RegisterManager<>("Schema meta id mgr for microservice " + microserviceName);
        intfSchemaMetaMgr = new RegisterManager<>("Schema meta interface mgr for microservice " + microserviceName);
    }

    public void regSchemaMeta(SchemaMeta schemaMeta) {
        idSchemaMetaMgr.register(schemaMeta.getSchemaId(), schemaMeta);
        intfSchemaMetaMgr.register(schemaMeta.getSwaggerIntf(), schemaMeta);

        for (OperationMeta operationMeta : schemaMeta.getOperations()) {
            regOperation(operationMeta.getSchemaQualifiedName(), operationMeta);
        }
    }

    public SchemaMeta ensureFindSchemaMeta(String schemaId) {
        return idSchemaMetaMgr.ensureFindValue(schemaId);
    }

    public SchemaMeta findSchemaMeta(String schemaId) {
        return idSchemaMetaMgr.findValue(schemaId);
    }

    public SchemaMeta ensureFindSchemaMeta(Class<?> schemaIntf) {
        return intfSchemaMetaMgr.ensureFindValue(schemaIntf);
    }

    public SchemaMeta findSchemaMeta(Class<?> schemaIntf) {
        return intfSchemaMetaMgr.findValue(schemaIntf);
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

    protected void parseMicroserviceName(String microserviceName) {
        int idxAt = microserviceName.indexOf(Const.APP_SERVICE_SEPARATOR);
        if (idxAt == -1) {
            appId = RegistryUtils.getMicroservice().getAppId();
            name = microserviceName;
            shortName = name;
            return;
        }

        appId = microserviceName.substring(0, idxAt);
        name = microserviceName;
        shortName = microserviceName.substring(idxAt + 1);
    }
}
