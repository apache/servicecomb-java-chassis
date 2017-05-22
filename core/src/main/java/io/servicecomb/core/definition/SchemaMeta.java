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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map.Entry;

import io.servicecomb.core.Handler;
import io.servicecomb.core.exception.ExceptionUtils;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import io.servicecomb.foundation.common.utils.ReflectUtils;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;

public class SchemaMeta extends CommonService<OperationMeta> {
    // 如果要生成class，使用这个package
    private String packageName;

    private Swagger swagger;

    private MicroserviceMeta microserviceMeta;

    // microserviceName:schemaId
    private String microserviceQualifiedName;

    // 契约对应的接口
    private Class<?> swaggerIntf;

    // handlerChain是microservice级别的
    private List<Handler> consumerHandlerChain;

    private List<Handler> providerHandlerChain;

    public SchemaMeta(Swagger swagger, MicroserviceMeta microserviceMeta, String schemaId) {
        this.packageName = SchemaUtils.generatePackageName(microserviceMeta, schemaId);

        this.swagger = swagger;
        this.name = schemaId;

        this.microserviceMeta = microserviceMeta;
        this.microserviceQualifiedName = microserviceMeta.getName() + "." + schemaId;
        // 确保swagger对应的接口是存在的
        swaggerIntf = ClassUtils.getOrCreateInterface(swagger, microserviceMeta.getClassLoader(), packageName);

        createOperationMgr("schemaMeta " + schemaId + " operation mgr");
        operationMgr.setRegisterErrorFmt("Operation name repeat, schema=%s, operation=%s");

        initOperations();
    }

    /**
     * 获取packageName的值
     * @return 返回 packageName
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     */
    private void initOperations() {
        for (Entry<String, Path> entry : swagger.getPaths().entrySet()) {
            String strPath = entry.getKey();
            Path path = entry.getValue();
            for (Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                Operation operation = operationEntry.getValue();
                if (operation.getOperationId() == null) {
                    throw ExceptionUtils.operationIdInvalid(getSchemaId(), strPath);
                }

                Method method = ReflectUtils.findMethod(swaggerIntf, operation.getOperationId());
                if (method == null) {
                    throw ExceptionUtils.operationNotExist(getSchemaId(), operation.getOperationId());
                }

                String httpMethod = operationEntry.getKey().name();
                OperationMeta operationMeta = new OperationMeta();
                operationMeta.init(this, method, strPath, httpMethod, operation);
                operationMgr.register(method.getName(), operationMeta);
            }
        }
    }

    /**
     * 获取swagger的值
     * @return 返回 swagger
     */
    public Swagger getSwagger() {
        return swagger;
    }

    public String getSchemaId() {
        return name;
    }

    /**
     * 获取microserviceQualifiedName的值
     * @return 返回 microserviceQualifiedName
     */
    public String getMicroserviceQualifiedName() {
        return microserviceQualifiedName;
    }

    /**
     * 获取microserviceName的值
     * @return 返回 microserviceName
     */
    public String getMicroserviceName() {
        return microserviceMeta.getName();
    }

    /**
     * 获取microserviceMeta的值
     * @return 返回 microserviceMeta
     */
    public MicroserviceMeta getMicroserviceMeta() {
        return microserviceMeta;
    }

    /**
     * 获取intf的值
     * @return 返回 intf
     */
    public Class<?> getSwaggerIntf() {
        return swaggerIntf;
    }

    /**
     * 获取consumerHandlerChain的值
     * @return 返回 consumerHandlerChain
     */
    public List<Handler> getConsumerHandlerChain() {
        return consumerHandlerChain;
    }

    /**
     * 对consumerHandlerChain进行赋值
     * @param consumerHandlerChain consumerHandlerChain的新值
     */
    public void setConsumerHandlerChain(List<Handler> consumerHandlerChain) {
        this.consumerHandlerChain = consumerHandlerChain;
    }

    /**
     * 获取providerHandlerChain的值
     * @return 返回 providerHandlerChain
     */
    public List<Handler> getProviderHandlerChain() {
        return providerHandlerChain;
    }

    /**
     * 对providerHandlerChain进行赋值
     * @param providerHandlerChain providerHandlerChain的新值
     */
    public void setProviderHandlerChain(List<Handler> providerHandlerChain) {
        this.providerHandlerChain = providerHandlerChain;
    }
}
