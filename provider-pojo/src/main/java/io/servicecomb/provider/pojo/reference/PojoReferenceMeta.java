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
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.provider.CseBeanPostProcessor.EmptyBeanPostProcessor;
import io.servicecomb.core.provider.consumer.ConsumerOperationMeta;
import io.servicecomb.core.provider.consumer.ReferenceConfig;
import io.servicecomb.provider.pojo.Invoker;

public class PojoReferenceMeta implements FactoryBean<Object>, InitializingBean, EmptyBeanPostProcessor {
    // 原始数据
    private String microserviceName;

    private String schemaId;

    private Class<?> consumerIntf;

    @Inject
    private PojoConsumers pojoConsumers;

    // 生成的数据
    private ReferenceConfig referenceConfig;

    private SchemaMeta schemaMeta;

    private Map<String, ConsumerOperationMeta> consumerOperationMap = new HashMap<>();

    // 根据intf创建出来的动态代理
    // TODO:未实现本地优先(本地场景下，应该跳过handler机制)
    private Object proxy;

    private Invoker invoker = new Invoker();

    private void prepare() {
        referenceConfig = CseContext.getInstance().getConsumerProviderManager().getReferenceConfig(microserviceName);
        MicroserviceMeta microserviceMeta = referenceConfig.getMicroserviceMeta();

        if (StringUtils.isEmpty(schemaId)) {
            // 未指定schemaId，看看consumer接口是否等于契约接口
            schemaMeta = microserviceMeta.findSchemaMeta(consumerIntf);
            if (schemaMeta == null) {
                // 尝试用consumer接口名作为schemaId
                schemaId = consumerIntf.getName();
                schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);
            }
        } else {
            schemaMeta = microserviceMeta.ensureFindSchemaMeta(schemaId);
        }

        if (consumerIntf == null) {
            consumerIntf = schemaMeta.getSwaggerIntf();
        }

        CseContext.getInstance().getConsumerSchemaFactory().connectToConsumer(schemaMeta,
                consumerIntf,
                consumerOperationMap);
    }

    public void createInvoker() {
        prepare();

        invoker.init(getReferenceConfig(),
                getSchemaMeta(),
                getConsumerOperationMap());
        createProxy();
    }

    protected void createProxy() {
        if (proxy == null) {
            proxy = Proxy.newProxyInstance(consumerIntf.getClassLoader(), new Class<?>[] {consumerIntf}, invoker);
        }
    }

    public Map<String, ConsumerOperationMeta> getConsumerOperationMap() {
        return consumerOperationMap;
    }

    public ReferenceConfig getReferenceConfig() {
        return referenceConfig;
    }

    public Object getProxy() {
        return getObject();
    }

    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    @Override
    public Object getObject() {
        if (proxy == null) {
            throw new Error("proxy is null");
        }
        return proxy;
    }

    @Override
    public Class<?> getObjectType() {
        return consumerIntf;
    }

    public Class<?> getConsumerIntf() {
        return consumerIntf;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setConsumerIntf(Class<?> intf) {
        this.consumerIntf = intf;
    }

    public SchemaMeta getSchemaMeta() {
        return schemaMeta;
    }

    public void setMicroserviceName(String microserviceName) {
        this.microserviceName = microserviceName;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    @Override
    public void afterPropertiesSet() {
        if (consumerIntf != null) {
            createProxy();
        }

        if (pojoConsumers != null) {
            pojoConsumers.addPojoReferenceMeta(this);
        }
    }
}
