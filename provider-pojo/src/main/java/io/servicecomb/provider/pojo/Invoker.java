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

package io.servicecomb.provider.pojo;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.swagger.engine.SwaggerConsumer;
import io.servicecomb.swagger.engine.SwaggerConsumerOperation;
import io.servicecomb.swagger.invocation.Response;
import io.servicecomb.swagger.invocation.exception.ExceptionFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Invoker implements InvocationHandler {
    private SchemaMeta schemaMeta;

    private ReferenceConfig config;

    private SwaggerConsumer swaggerConsumer;

    public void init(ReferenceConfig config, SchemaMeta schemaMeta,
            SwaggerConsumer swaggerConsumer) {
        this.config = config;
        this.schemaMeta = schemaMeta;
        this.swaggerConsumer = swaggerConsumer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Invocation invocation =
            InvocationFactory.forConsumer(config, schemaMeta, method.getName(), null);

        SwaggerConsumerOperation consumerOperation = swaggerConsumer.findOperation(method.getName());
        consumerOperation.getArgumentsMapper().toInvocation(args, invocation);

        Response response = InvokerUtils.innerSyncInvoke(invocation);
        if (response.isSuccessed()) {
            return consumerOperation.getResponseMapper().mapResponse(response);
        }

        throw ExceptionFactory.convertConsumerException(response.getResult());
    }
}
