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

package io.servicecomb.provider.pojo.reference;

import java.lang.reflect.Proxy;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import io.servicecomb.core.provider.CseBeanPostProcessor.EmptyBeanPostProcessor;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.provider.pojo.Invoker;

public class PojoReferenceMeta implements FactoryBean<Object>, InitializingBean, EmptyBeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PojoReferenceMeta.class);

    // 原始数据
    private String microserviceName;

    private String schemaId;

    private Class<?> consumerIntf;

    @Inject
    private PojoConsumers pojoConsumers;

    // 根据intf创建出来的动态代理
    // TODO:未实现本地优先(本地场景下，应该跳过handler机制)
    private Object proxy;

    private Invoker invoker;

    public void createInvoker() {
        // only consumerIntf is null need to do query contract during boot
        if (consumerIntf != null) {
            return;
        }

        invoker.prepare();
        this.consumerIntf = invoker.getConsumerIntf();
        createProxy();
    }

    protected void createProxy() {
        proxy = Proxy.newProxyInstance(consumerIntf.getClassLoader(), new Class<?>[] {consumerIntf}, invoker);
    }

    public Object getProxy() {
        return getObject();
    }

    @Override
    public Object getObject() {
        if (proxy == null) {
            throw new ServiceCombException(
                    String.format("Rpc reference %s with service name [%s] and schema [%s] is not populated",
                            consumerIntf == null ? "" : consumerIntf,
                            microserviceName,
                            schemaId));
        }
        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return consumerIntf;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setConsumerIntf(Class<?> intf) {
        this.consumerIntf = intf;
    }

    public void setMicroserviceName(String microserviceName) {
        this.microserviceName = microserviceName;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    @Override
    public void afterPropertiesSet() {
        invoker = new Invoker(microserviceName, schemaId, consumerIntf);
        if (consumerIntf != null) {
            createProxy();
        } else {
            LOGGER.warn("Deprecated usage. xml definition cse:rpc-reference missed \"interface\" property, "
                    + "to support this, must query schema ids from service center in blocking mode during boot until got it, "
                    + "if there is loop dependency between microservices, will cause the microservices can not boot.");
        }

        if (pojoConsumers != null) {
            pojoConsumers.addPojoReferenceMeta(this);
        }
    }
}
