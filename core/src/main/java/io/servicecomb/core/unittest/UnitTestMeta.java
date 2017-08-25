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

package io.servicecomb.core.unittest;

import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.servicecomb.core.CseContext;
import io.servicecomb.core.Handler;
import io.servicecomb.core.definition.MicroserviceMeta;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.definition.loader.SchemaListenerManager;
import io.servicecomb.core.definition.loader.SchemaLoader;
import io.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import io.servicecomb.core.handler.ConsumerHandlerManager;
import io.servicecomb.core.handler.ProducerHandlerManager;
import io.servicecomb.core.handler.config.Config;
import io.servicecomb.core.handler.impl.SimpleLoadBalanceHandler;
import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import io.swagger.models.Swagger;
import mockit.Mock;
import mockit.MockUp;

public class UnitTestMeta {
  private static boolean inited = false;

  private static MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();

  private static SchemaListenerManager schemaListenerManager = new SchemaListenerManager();

  @SuppressWarnings("unchecked")
  public static synchronized void init() {
    if (inited) {
      return;
    }

    ConsumerProviderManager consumerProviderManager = new ConsumerProviderManager();

    ConsumerSchemaFactory consumerSchemaFactory = new ConsumerSchemaFactory() {
      @Override
      protected Microservice findMicroservice(MicroserviceMeta microserviceMeta,
          String microserviceVersionRule) {
        return null;
      }
    };
    consumerSchemaFactory.setMicroserviceMetaManager(microserviceMetaManager);
    consumerSchemaFactory.setSchemaListenerManager(schemaListenerManager);

    consumerProviderManager.setConsumerSchemaFactory(consumerSchemaFactory);

    CseContext.getInstance().setConsumerProviderManager(consumerProviderManager);
    CseContext.getInstance().setConsumerSchemaFactory(consumerSchemaFactory);
    CseContext.getInstance().setSchemaListenerManager(schemaListenerManager);

    Config config = new Config();
    Class<?> cls = SimpleLoadBalanceHandler.class;
    config.getHandlerClassMap().put("simpleLB", (Class<Handler>) cls);
    ProducerHandlerManager.INSTANCE.init(config);
    ConsumerHandlerManager.INSTANCE.init(config);

    ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    Mockito.when(applicationContext.getBean(Mockito.anyString())).thenReturn(null);
    BeanUtils.setContext(applicationContext);
    inited = true;
  }

  static {
    init();
  }

  private SchemaLoader schemaLoader = new SchemaLoader() {
    public void putSelfBasePathIfAbsent(String microserviceName, String basePath) {
    }
  };


  public UnitTestMeta() {

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
