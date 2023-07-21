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
package org.apache.servicecomb.serviceregistry.swagger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.registry.api.registry.Microservice;
import org.apache.servicecomb.serviceregistry.TestRegistryBase;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import io.swagger.v3.oas.models.OpenAPI;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

public class TestSwaggerLoader extends TestRegistryBase {

  @Test
  public void registerSwagger() {
    OpenAPI swagger = SwaggerGenerator.generate(Hello.class);
    RegistrationManager.INSTANCE.getSwaggerLoader().registerSwagger("default:ms2", schemaId, swagger);

    Microservice microservice = appManager.getOrCreateMicroserviceVersions(appId, serviceName)
        .getVersions().values().iterator().next().getMicroservice();
    Assertions.assertSame(swagger, RegistrationManager.INSTANCE
        .getSwaggerLoader().loadSwagger(microservice, null, schemaId));
  }

  @Test
  public void loadFromRemote() {
    OpenAPI swagger = SwaggerGenerator.generate(Hello.class);
    String swaggerString = SwaggerUtils.swaggerToString(swagger);
    OpenAPI swaggerOpenApi = SwaggerUtils.parseSwagger(swaggerString);

    new Expectations(serviceRegistry.getServiceRegistryClient()) {
      {
        serviceRegistry.getServiceRegistryClient().getAggregatedSchema(serviceId, schemaId);
        result = SwaggerUtils.swaggerToString(swagger);
      }
    };

    RegistrationManager.INSTANCE.getSwaggerLoader().unregisterSwagger(appId, serviceName, schemaId);

    Microservice microservice = appManager.getOrCreateMicroserviceVersions(appId, serviceName)
        .getVersions().values().iterator().next().getMicroservice();
    OpenAPI loadedSwagger = RegistrationManager.INSTANCE
        .getSwaggerLoader().loadSwagger(microservice, null, schemaId);
    Assertions.assertNotSame(swagger, loadedSwagger);
    // OpenApi -> String -> OpenApi, maybe not produce same OpenApi instance.
    Assertions.assertEquals(swaggerOpenApi, loadedSwagger);
//    Assertions.assertEquals(swagger, loadedSwagger);
  }

  @Test
  public void loadFromResource_sameApp_dirWithoutApp() {
    OpenAPI swagger = SwaggerGenerator.generate(Hello.class);
    String swaggerString = SwaggerUtils.swaggerToString(swagger);
    OpenAPI swaggerOpenApi = SwaggerUtils.parseSwagger(swaggerString);
    mockLocalResource(swagger, String.format("microservices/%s/%s.yaml", serviceName, schemaId));

    RegistrationManager.INSTANCE.getSwaggerLoader().unregisterSwagger(appId, serviceName, schemaId);

    Microservice microservice = appManager.getOrCreateMicroserviceVersions(appId, serviceName)
        .getVersions().values().iterator().next().getMicroservice();
    OpenAPI loadedSwagger = RegistrationManager.INSTANCE
        .getSwaggerLoader().loadSwagger(microservice, null, schemaId);
    Assertions.assertNotSame(swagger, loadedSwagger);
    // OpenApi -> String -> OpenApi, maybe not produce same OpenApi instance.
    Assertions.assertEquals(swaggerOpenApi, loadedSwagger);
//    Assertions.assertEquals(swagger, loadedSwagger);
  }

  @Test
  public void loadFromResource_sameApp_dirWithApp() {
    OpenAPI swagger = SwaggerGenerator.generate(Hello.class);
    String swaggerString = SwaggerUtils.swaggerToString(swagger);
    OpenAPI swaggerOpenApi = SwaggerUtils.parseSwagger(swaggerString);

    mockLocalResource(swagger, String.format("applications/%s/%s/%s.yaml", appId, serviceName, schemaId));

    RegistrationManager.INSTANCE.getSwaggerLoader().unregisterSwagger(appId, serviceName, schemaId);

    Microservice microservice = appManager.getOrCreateMicroserviceVersions(appId, serviceName)
        .getVersions().values().iterator().next().getMicroservice();
    OpenAPI loadedSwagger = RegistrationManager.INSTANCE
        .getSwaggerLoader().loadSwagger(microservice, null, schemaId);
    Assertions.assertNotSame(swagger, loadedSwagger);
    // OpenApi -> String -> OpenApi, maybe not produce same OpenApi instance.
    Assertions.assertEquals(swaggerOpenApi, loadedSwagger);
//    Assertions.assertEquals(swagger, loadedSwagger);
  }

  @Test
  public void loadFromResource_diffApp_dirWithoutApp() {
    OpenAPI swagger = SwaggerGenerator.generate(Hello.class);
    mockLocalResource(swagger, String.format("microservices/%s/%s.yaml", "ms3", schemaId));

    Microservice microservice = appManager.getOrCreateMicroserviceVersions("other", "ms3")
        .getVersions().values().iterator().next().getMicroservice();

    Assertions.assertNull(RegistrationManager.INSTANCE.getSwaggerLoader().loadSwagger(microservice, null, schemaId));
  }

  @Test
  public void loadFromResource_diffApp_dirWithApp() {
    OpenAPI swagger = SwaggerGenerator.generate(Hello.class);
    String swaggerString = SwaggerUtils.swaggerToString(swagger);
    OpenAPI swaggerOpenApi = SwaggerUtils.parseSwagger(swaggerString);

    mockLocalResource(swagger, String.format("applications/%s/%s/%s.yaml", "other", "ms3", schemaId));

    Microservice microservice = appManager.getOrCreateMicroserviceVersions("other", "ms3")
        .getVersions().values().iterator().next().getMicroservice();
    OpenAPI loadedSwagger = RegistrationManager.INSTANCE
        .getSwaggerLoader().loadSwagger(microservice, null, schemaId);
    Assertions.assertNotSame(swagger, loadedSwagger);
    // OpenApi -> String -> OpenApi, maybe not produce same OpenApi instance.
    Assertions.assertEquals(swaggerOpenApi, loadedSwagger);
//    Assertions.assertEquals(swagger, loadedSwagger);
  }

  private void mockLocalResource(OpenAPI swagger, String path) {
    mockLocalResource(SwaggerUtils.swaggerToString(swagger), path);
  }

  private void mockLocalResource(String content, String path) {
    Map<String, String> resourceMap = new HashMap<>();
    resourceMap.put(path, content);

    mockLocalResource(resourceMap);
  }

  private void mockLocalResource(Map<String, String> resourceMap) {
    Holder<String> pathHolder = new Holder<>();
    URL url = new MockUp<URL>() {
      @Mock
      String getPath() {
        return pathHolder.value;
      }

      @Mock
      String toExternalForm() {
        return pathHolder.value;
      }
    }.getMockInstance();
    ClassLoader classLoader = new MockUp<ClassLoader>() {
      @Mock
      URL getResource(String name) {
        pathHolder.value = name;
        return resourceMap.containsKey(name) ? url : null;
      }

      @Mock
      InputStream getResourceAsStream(String name) {
        String content = resourceMap.get(name);
        return content == null ? null : new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
      }
    }.getMockInstance();
    new Expectations(JvmUtils.class) {
      {
        JvmUtils.findClassLoader();
        result = classLoader;
      }
    };
    new MockUp<IOUtils>() {
      @Mock
      String toString(URL url, Charset encoding) {
        return resourceMap.get(url.getPath());
      }
    };
  }
}
