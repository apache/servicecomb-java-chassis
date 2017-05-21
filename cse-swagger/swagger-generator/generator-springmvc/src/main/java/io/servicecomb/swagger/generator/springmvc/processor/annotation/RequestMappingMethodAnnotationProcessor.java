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

package io.servicecomb.swagger.generator.springmvc.processor.annotation;

import java.util.Arrays;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.servicecomb.swagger.generator.core.MethodAnnotationProcessor;
import io.servicecomb.swagger.generator.core.OperationGenerator;
import io.swagger.models.Operation;

public class RequestMappingMethodAnnotationProcessor implements MethodAnnotationProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(Object annotation, OperationGenerator operationGenerator) {
        RequestMapping requestMapping = (RequestMapping) annotation;
        Operation operation = operationGenerator.getOperation();

        // path/value是等同的
        this.processPath(requestMapping.path(), operationGenerator);
        this.processPath(requestMapping.value(), operationGenerator);
        this.processMethod(requestMapping.method(), operationGenerator);

        this.processConsumes(requestMapping.consumes(), operation);
        this.processProduces(requestMapping.produces(), operation);
    }

    protected void processPath(String[] paths, OperationGenerator operationGenerator) {
        if (null == paths || paths.length == 0) {
            return;
        }

        // swagger仅支持配一个path，否则将会出现重复的operationId
        if (paths.length > 1) {
            throw new Error(String.format("not allowed multi path for %s:%s",
                    operationGenerator.getProviderMethod().getDeclaringClass().getName(),
                    operationGenerator.getProviderMethod().getName()));
        }

        operationGenerator.setPath(paths[0]);
    }

    protected void processMethod(RequestMethod[] requestMethods, OperationGenerator operationGenerator) {
        if (null == requestMethods || requestMethods.length == 0) {
            return;
        }

        if (requestMethods.length > 1) {
            throw new Error(
                    String.format("not allowed multi http method for %s:%s",
                            operationGenerator.getProviderMethod().getDeclaringClass().getName(),
                            operationGenerator.getProviderMethod().getName()));
        }

        operationGenerator.setHttpMethod(requestMethods[0].name());
    }

    private void processConsumes(String[] consumes, Operation operation) {
        if (null == consumes || consumes.length == 0) {
            return;
        }

        operation.setConsumes(Arrays.asList(consumes));
    }

    protected void processProduces(String[] produces, Operation operation) {
        if (null == produces || produces.length == 0) {
            return;
        }

        operation.setProduces(Arrays.asList(produces));
    }
}
