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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.executor.ExecutorManager;
import com.huawei.paas.cse.swagger.invocation.response.ResponseMeta;
import com.huawei.paas.cse.swagger.invocation.response.ResponsesMeta;

import io.swagger.models.Operation;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年11月30日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class OperationMeta {
    private SchemaMeta schemaMeta;

    // schemaId:operation
    private String schemaQualifiedName;

    // microserviceName:schemaId:operation
    private String microserviceQualifiedName;

    // 契约对应的method，与consumer、producer的method没有必然关系
    private Method method;

    private boolean sync;

    private String httpMethod;

    private String operationPath;

    private Operation swaggerOperation;

    // 在哪个executor上执行
    private Executor executor;

    private ResponsesMeta responsesMeta = new ResponsesMeta();

    // transport、provider、consumer端都可能需要扩展数据
    // 为避免每个地方都做复杂的层次管理，直接在这里保存扩展数据
    private Map<String, Object> extData = new ConcurrentHashMap<>();

    public void init(SchemaMeta schemaMeta, Method method, String operationPath, String httpMethod,
            Operation swaggerOperation) {
        this.schemaMeta = schemaMeta;
        schemaQualifiedName = schemaMeta.getSchemaId() + "." + method.getName();
        microserviceQualifiedName = schemaMeta.getMicroserviceName() + "." + schemaQualifiedName;
        this.operationPath = operationPath;
        this.method = method;
        this.httpMethod = httpMethod.toUpperCase(Locale.US);
        this.swaggerOperation = swaggerOperation;
        executor = ExecutorManager.findExecutor(this);

        collectMethodType();

        responsesMeta.init(schemaMeta.getMicroserviceMeta().getClassLoader(),
                schemaMeta.getPackageName(),
                schemaMeta.getSwagger(),
                swaggerOperation,
                method.getGenericReturnType());
    }

    /**
    * 获取httpMethod的值
    * @return 返回 httpMethod
    */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * 对httpMethod进行赋值
     * @param httpMethod httpMethod的新值
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
    * 获取operationPath的值
    * @return 返回 operationPath
    */
    public String getOperationPath() {
        return operationPath;
    }

    private void collectMethodType() {
        Class<?>[] params = method.getParameterTypes();
        if (params.length == 0) {
            sync = true;
            return;
        }

        Class<?> lastParam = params[params.length - 1];
        sync = !AsyncResponse.class.isAssignableFrom(lastParam);
    }

    /**
     * 获取swaggerOperation的值
     * @return 返回 swaggerOperation
     */
    public Operation getSwaggerOperation() {
        return swaggerOperation;
    }

    public ResponseMeta findResponseMeta(int statusCode) {
        return responsesMeta.findResponseMeta(statusCode);
    }

    /**
     * 获取schemaMeta的值
     * @return 返回 schemaMeta
     */
    public SchemaMeta getSchemaMeta() {
        return schemaMeta;
    }

    /**
     * 获取schemaQualifiedName的值
     * @return 返回 schemaQualifiedName
     */
    public String getSchemaQualifiedName() {
        return schemaQualifiedName;
    }

    /**
     * 获取microserviceQualifiedName的值
     * @return 返回 microserviceQualifiedName
     */
    public String getMicroserviceQualifiedName() {
        return microserviceQualifiedName;
    }

    public String getMicroserviceName() {
        return schemaMeta.getMicroserviceName();
    }

    /**
     * 获取method的值
     * @return 返回 method
     */
    public Method getMethod() {
        return method;
    }

    public String getOperationId() {
        return swaggerOperation.getOperationId();
    }

    // 调用者保证参数正确性
    public String getParamName(int idx) {
        return swaggerOperation.getParameters().get(idx).getName();
    }

    public void putExtData(String key, Object data) {
        extData.put(key, data);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtData(String key) {
        return (T) extData.get(key);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @return
     */
    public boolean isSync() {
        return sync;
    }

    /**
     * 获取executor的值
     * @return 返回 executor
     */
    public Executor getExecutor() {
        return executor;
    }

    /**
     * 对executor进行赋值
     * @param executor executor的新值
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }
}
