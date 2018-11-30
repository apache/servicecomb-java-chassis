/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.core.unittest;

import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.PrivateMicroserviceVersionMetaFactory;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.definition.loader.SchemaListenerManager;
import org.apache.servicecomb.core.definition.loader.SchemaLoader;
import org.apache.servicecomb.core.definition.schema.ConsumerSchemaFactory;
import org.apache.servicecomb.core.handler.ConsumerHandlerManager;
import org.apache.servicecomb.core.handler.ProducerHandlerManager;
import org.apache.servicecomb.core.handler.config.Config;
import org.apache.servicecomb.core.handler.impl.SimpleLoadBalanceHandler;
import org.apache.servicecomb.core.provider.consumer.ConsumerProviderManager;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import org.apache.servicecomb.serviceregistry.registry.ServiceRegistryFactory;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.swagger.models.Swagger;
import mockit.Mock;
import mockit.MockUp;

/**
 * when SCBEngine finished, UnitTestMeta will be deleted
 */
public class UnitTestMeta {

  private SchemaListenerManager schemaListenerManager = new SchemaListenerManager();

  private ConsumerProviderManager consumerProviderManager;

  public ConsumerProviderManager getConsumerProviderManager() {
    return consumerProviderManager;
  }

  private ConsumerSchemaFactory consumerSchemaFactory;

  private SchemaLoader schemaLoader = new SchemaLoader();

  private MicroserviceMeta microserviceMeta;

  private ServiceRegistry serviceRegistry;

  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  public ConsumerSchemaFactory getConsumerSchemaFactory() {
    return consumerSchemaFactory;
  }

  @SuppressWarnings("unchecked")
  public void init() {
    serviceRegistry = ServiceRegistryFactory.createLocal();
    serviceRegistry.init();
    serviceRegistry.getAppManager().setMicroserviceVersionFactory(new PrivateMicroserviceVersionMetaFactory());
    RegistryUtils.setServiceRegistry(serviceRegistry);
    microserviceMeta = new MicroserviceMeta(RegistryUtils.getMicroservice().getServiceName());
    SCBEngine.getInstance().setProducerMicroserviceMeta(microserviceMeta);
    consumerProviderManager = new ConsumerProviderManager();

    consumerSchemaFactory = new ConsumerSchemaFactory();

    consumerSchemaFactory.setSchemaLoader(schemaLoader);

    consumerProviderManager.setAppManager(RegistryUtils.getServiceRegistry().getAppManager());

    CseContext.getInstance().setConsumerProviderManager(consumerProviderManager);
    CseContext.getInstance().setConsumerSchemaFactory(consumerSchemaFactory);
    CseContext.getInstance().setSchemaListenerManager(schemaListenerManager);

    Config config = new Config();
    Class<?> cls = SimpleLoadBalanceHandler.class;
    config.getHandlerClassMap().put("simpleLB", (Class<Handler>) cls);
    ProducerHandlerManager.INSTANCE.init(new Config());
    ConsumerHandlerManager.INSTANCE.init(config);

    ApplicationContext applicationContext = Mockito.mock(ApplicationContext.class);
    Mockito.when(applicationContext.getBean(Mockito.anyString())).thenReturn(null);
    BeanUtils.setContext(applicationContext);
  }

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

    init();
  }

  public SchemaMeta getOrCreateSchemaMeta(Class<?> impl) {
    return getOrCreateSchemaMeta("app", "test", impl.getName(), impl);
  }

  public SchemaMeta getOrCreateSchemaMeta(String appId, String microserviceName, String schemaId, Class<?> impl) {
    MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();
    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta(schemaId);
    if (schemaMeta != null) {
      return schemaMeta;
    }

    Swagger swagger = UnitTestSwaggerUtils.generateSwagger(impl).getSwagger();
    return schemaLoader.registerSchema(microserviceMeta, schemaId, swagger);
  }

  public void registerSchema(SwaggerGeneratorContext swaggerGeneratorContext, Class<?> schemaCls) {
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(swaggerGeneratorContext, schemaCls);
    swaggerGenerator.setClassLoader(new ClassLoader() {
    });
    Swagger swagger = swaggerGenerator.generate();

    Microservice microservice = new Microservice();
    microservice.setAppId("app");
    microservice.setServiceName("app:test");
    microservice.setVersion("1.0.0");
    microservice.getSchemas().add(schemaCls.getName());
    microservice.setServiceId(serviceRegistry.getServiceRegistryClient().registerMicroservice(microservice));

    serviceRegistry.getServiceRegistryClient()
        .registerSchema(microservice.getServiceId(), schemaCls.getName(),
            SwaggerUtils.swaggerToString(swagger));

    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setServiceId(microservice.getServiceId());
    instance.setInstanceId(serviceRegistry.getServiceRegistryClient().registerMicroserviceInstance(instance));
  }
}
