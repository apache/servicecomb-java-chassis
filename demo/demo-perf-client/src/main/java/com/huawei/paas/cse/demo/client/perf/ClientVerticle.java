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

package com.huawei.paas.cse.demo.client.perf;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.exception.CommonExceptionData;
import io.servicecomb.core.exception.InvocationException;
import io.servicecomb.core.invocation.InvocationFactory;
import io.servicecomb.core.provider.consumer.InvokerUtils;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import com.huawei.paas.cse.demo.pojo.client.PojoClient;
import com.huawei.paas.cse.demo.server.Test;
import com.huawei.paas.cse.demo.server.TestRequest;
import com.huawei.paas.cse.demo.server.User;

import io.vertx.core.AbstractVerticle;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author   
 * @version  [版本号, 2017年2月16日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class ClientVerticle extends AbstractVerticle {

    Test test = PojoClient.test;

    ReferenceConfig config =
        CseContext.getInstance().getConsumerProviderManager().setTransport("pojo", Config.getTransport());

    int idx = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws Exception {

        vertx.setTimer(100, this::send);
    }

    protected void send(Long event) {
        User user = new User();

        TestRequest request = new TestRequest();
        request.setUser(user);
        request.setIndex(idx);
        request.setData(PojoClient.buffer);

        SchemaMeta schemaMeta = config.getMicroserviceMeta().ensureFindSchemaMeta("server");
        Object[] args = new Object[] {request};
        Invocation invocation = InvocationFactory.forConsumer(config, schemaMeta, "wrapParam", args);
        InvokerUtils.reactiveInvoke(invocation, ar -> {
            if (ar.isSuccessed()) {
                User result = ar.getResult();
                if (result.getIndex() != idx) {
                    System.out.printf("error result:%s, expect idx %d\n", result, idx);
                }
            } else {
                CommonExceptionData data =
                    (CommonExceptionData) ((InvocationException) ar.getResult()).getErrorData();
                System.out.println(data.getMessage());
            }

            send(null);
        });
    }
}
