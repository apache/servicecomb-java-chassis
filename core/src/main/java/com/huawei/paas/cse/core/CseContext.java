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

package com.huawei.paas.cse.core;

import javax.inject.Inject;

import com.huawei.paas.cse.core.context.HttpStatusManager;
import com.huawei.paas.cse.core.definition.MicroserviceMetaManager;
import com.huawei.paas.cse.core.definition.loader.SchemaListenerManager;
import com.huawei.paas.cse.core.definition.loader.SchemaLoader;
import com.huawei.paas.cse.core.definition.schema.ConsumerSchemaFactory;
import com.huawei.paas.cse.core.provider.consumer.ConsumerProviderManager;
import com.huawei.paas.cse.core.provider.producer.ProducerProviderManager;
import com.huawei.paas.cse.core.transport.TransportManager;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月7日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class CseContext {
    private static final CseContext INSTANCE = new CseContext();

    public static CseContext getInstance() {
        return INSTANCE;
    }

    private SchemaListenerManager schemaListenerManager;

    private SchemaLoader schemaLoader;

    private MicroserviceMetaManager microserviceMetaManager;

    private ConsumerSchemaFactory consumerSchemaFactory;

    private ConsumerProviderManager consumerProviderManager;

    private ProducerProviderManager producerProviderManager;

    private TransportManager transportManager;

    private HttpStatusManager statusMgr = new HttpStatusManager();

    public SchemaListenerManager getSchemaListenerManager() {
        return schemaListenerManager;
    }

    public SchemaLoader getSchemaLoader() {
        return schemaLoader;
    }

    public MicroserviceMetaManager getMicroserviceMetaManager() {
        return microserviceMetaManager;
    }

    public ConsumerSchemaFactory getConsumerSchemaFactory() {
        return consumerSchemaFactory;
    }

    public ConsumerProviderManager getConsumerProviderManager() {
        return consumerProviderManager;
    }

    public ProducerProviderManager getProducerProviderManager() {
        return producerProviderManager;
    }

    public TransportManager getTransportManager() {
        return transportManager;
    }

    public HttpStatusManager getStatusMgr() {
        return statusMgr;
    }

    @Inject
    public void setMicroserviceMetaManager(MicroserviceMetaManager microserviceMetaManager) {
        this.microserviceMetaManager = microserviceMetaManager;
    }

    @Inject
    public void setSchemaLoader(SchemaLoader schemaLoader) {
        this.schemaLoader = schemaLoader;
    }

    @Inject
    public void setConsumerSchemaFactory(ConsumerSchemaFactory consumerSchemaFactory) {
        this.consumerSchemaFactory = consumerSchemaFactory;
    }

    @Inject
    public void setConsumerProviderManager(ConsumerProviderManager consumerProviderManager) {
        this.consumerProviderManager = consumerProviderManager;
    }

    @Inject
    public void setProducerProviderManager(ProducerProviderManager producerProviderManager) {
        this.producerProviderManager = producerProviderManager;
    }

    @Inject
    public void setSchemaListenerManager(SchemaListenerManager schemaListenerManager) {
        this.schemaListenerManager = schemaListenerManager;
    }

    @Inject
    public void setTransportManager(TransportManager transportManager) {
        this.transportManager = transportManager;
    }
}
