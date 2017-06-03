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

package io.servicecomb.swagger.invocation.arguments.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.servicecomb.swagger.generator.core.SwaggerGenerator;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import io.servicecomb.swagger.invocation.arguments.ArgumentsMapperFactory;
import io.servicecomb.foundation.common.utils.ReflectUtils;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;

public class OpMeta<ARGS_MAPPER> {
    protected SwaggerGenerator providerGenerator;

    protected Swagger swagger;

    protected ArgumentsMapperFactory factory;

    protected Map<String, ARGS_MAPPER> operationArgsMapperMap = new HashMap<>();

    protected Map<String, Method> swaggerMethodMap = new HashMap<>();

    public ARGS_MAPPER findArgsMapper(String methodName) {
        return operationArgsMapperMap.get(methodName);
    }

    public Method findSwaggerMethod(String methodName) {
        return swaggerMethodMap.get(methodName);
    }

    protected void init() {
        Class<?> swaggerInterface = ClassUtils.getOrCreateInterface(swagger, null, null);

        for (OperationGenerator operationGenerator : providerGenerator.getOperationGeneratorMap().values()) {
            String methodName = operationGenerator.getProviderMethod().getName();

            Method swaggerMethod = ReflectUtils.findMethod(swaggerInterface, methodName);
            List<Parameter> swaggerParameters = findParameter(swagger, methodName);

            List<Parameter> providerParameters = findParameter(providerGenerator, methodName);

            ARGS_MAPPER argsMapper = factory.createArgumentsMapper(swagger,
                    swaggerMethod,
                    swaggerParameters,
                    operationGenerator.getProviderMethod(),
                    providerParameters);

            swaggerMethodMap.put(methodName, swaggerMethod);
            operationArgsMapperMap.put(methodName, argsMapper);
        }
    }

    private List<Parameter> findParameter(SwaggerGenerator generator, String methodName) {
        OperationGenerator operationGeneraotr = generator.getOperationGeneratorMap().get(methodName);
        if (operationGeneraotr == null) {
            throw new Error("method not found, name=" + methodName);
        }

        return operationGeneraotr.getProviderParameters();
    }

    private List<Parameter> findParameter(Swagger swagger, String methodName) {
        for (Path path : swagger.getPaths().values()) {
            for (Operation operation : path.getOperations()) {
                if (methodName.equals(operation.getOperationId())) {
                    return operation.getParameters();
                }
            }
        }

        throw new Error("method not found, name=" + methodName);
    }
}
