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

package com.huawei.paas.cse.core.unittest;

import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import com.huawei.paas.cse.core.Handler;
import com.huawei.paas.cse.core.definition.MicroserviceMeta;
import com.huawei.paas.cse.core.definition.MicroserviceMetaManager;
import com.huawei.paas.cse.core.definition.SchemaMeta;
import com.huawei.paas.cse.core.definition.loader.SchemaLoader;
import com.huawei.paas.cse.core.handler.ConsumerHandlerManager;
import com.huawei.paas.cse.core.handler.ProducerHandlerManager;
import com.huawei.paas.cse.core.handler.config.Config;
import com.huawei.paas.cse.core.handler.impl.SimpleLoadBalanceHandler;
import com.huawei.paas.cse.serviceregistry.RegistryUtils;
import com.huawei.paas.cse.serviceregistry.api.registry.Microservice;
import com.huawei.paas.cse.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import com.huawei.paas.foundation.common.utils.BeanUtils;

import io.swagger.models.Swagger;
import mockit.Mock;
import mockit.MockUp;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月11日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class UnitTestMeta {
    private static boolean inited = false;

    @SuppressWarnings("unchecked")
    public static synchronized void init() {
        if (inited) {
            return;
        }

        Config config = new Config();
        Class<?> cls = SimpleLoadBalanceHandler.class;
        config.getHandlerClassMap().put("simpleLB", (Class<Handler>) cls);
        ProducerHandlerManager.INSTANCE.init(config);
        ConsumerHandlerManager.INSTANCE.init(config);

        ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
        BeanUtils.setContext(applicationContext);
        inited = true;
    }

    static {
        init();
    }

    private MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();

    private SchemaLoader schemaLoader = new SchemaLoader() {
        public void putSelfBasePathIfAbsent(String microserviceName, String basePath) {
        };
    };

    private Microservice microservice = new Microservice();

    public UnitTestMeta() {
        microservice.setAppId("app");
        microservice.setServiceName("testname");
        new MockUp<RegistryUtils>() {
            @Mock
            private Microservice createMicroserviceFromDefinition() {
                return microservice;
            }
        };

        new MockUp<ConsumerHandlerManager>() {
            @Mock
            public List<Handler> getOrCreate(String name) {
                return Collections.emptyList();
            }
        };
        new MockUp<ProducerHandlerManager>() {
            @Mock
            public List<Handler> getOrCreate(String name) {
                return Collections.emptyList();
            }
        };

        schemaLoader.setMicroserviceMetaManager(microserviceMetaManager);
    }

    public void setMicroservice(Microservice microservice) {
        this.microservice = microservice;
    }

    public MicroserviceMetaManager getMicroserviceMetaManager() {
        return microserviceMetaManager;
    }

    public SchemaMeta getOrCreateSchemaMeta(Class<?> impl) {
        return getOrCreateSchemaMeta("app", "test", impl.getName(), impl);
    }

    public SchemaMeta getOrCreateSchemaMeta(String appId, String microserviceName, String schemaId, Class<?> impl) {
        String longName = appId + ":" + microserviceName;
        MicroserviceMeta microserviceMeta = microserviceMetaManager.getOrCreateMicroserviceMeta(longName);
        SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta(schemaId);
        if (schemaMeta != null) {
            return schemaMeta;
        }

        Swagger swagger = UnitTestSwaggerUtils.generateSwagger(impl).getSwagger();
        return schemaLoader.registerSchema(microserviceMeta, schemaId, swagger);
    }
}
