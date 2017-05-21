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

package io.servicecomb.core.provider.producer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.core.AsyncResponse;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.Response;
import io.servicecomb.core.context.ContextUtils;
import io.servicecomb.core.context.InvocationContext;
import io.servicecomb.core.exception.InvocationException;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月1日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ProducerOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerOperation.class);

    // 本method所属实例
    private Object instance;

    private Method method;

    private ProducerArgumentsMapper argsMapper;

    private ProducerResponseMapper responseMapper;

    public ProducerOperation(Object instance, Method method, ProducerArgumentsMapper argsMapper,
            ProducerResponseMapper responseMapper) {
        this.instance = instance;
        this.method = method;
        this.argsMapper = argsMapper;
        this.responseMapper = responseMapper;
    }

    public void invoke(Invocation invocation, AsyncResponse asyncResp) {
        InvocationContext context = new InvocationContext(invocation.getContext());
        ContextUtils.setInvocationContext(context);
        Response response = null;
        try {
            Object[] args = argsMapper.toProducerArgs(invocation);
            Object result = method.invoke(instance, args);
            response = responseMapper.mapResponse(context.getStatus(), result);
        } catch (Throwable e) {
            response = processException(e);
        }
        ContextUtils.removeInvocationContext();

        asyncResp.handle(response);
    }

    protected Response processException(Throwable e) {
        if (InvocationTargetException.class.isInstance(e)) {
            e = ((InvocationTargetException) e).getTargetException();
        }

        if (InvocationException.class.isInstance(e)) {
            return Response.failResp((InvocationException) e);
        }

        // 未知异常，记录下来方便定位问题
        Response response = Response.producerFailResp(e);
        String msg =
            String.format("Producer invoke failed, %s:%s", method.getDeclaringClass().getName(), method.getName());
        LOGGER.error(msg, e);
        return response;
    }
}
