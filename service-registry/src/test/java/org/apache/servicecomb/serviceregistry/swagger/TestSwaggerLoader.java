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

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.serviceregistry.TestRegistryBase;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.models.Swagger;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

public class TestSwaggerLoader extends TestRegistryBase {
  @Test
  public void registerSwagger() {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    serviceRegistry.getSwaggerLoader().registerSwagger("default:ms2", schemaId, swagger);

    Assert.assertSame(swagger,
        appManager.getOrCreateMicroserviceVersions(appId, serviceName)
            .getVersions().values().iterator().next()
            .getMicroserviceMeta()
            .findSchemaMeta(schemaId)
            .getSwagger());
  }

  @Test
  public void loadFromRemote() {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    new Expectations(serviceRegistry.getServiceRegistryClient()) {
      {
        serviceRegistry.getServiceRegistryClient().getAggregatedSchema(serviceId, schemaId);
        result = SwaggerUtils.swaggerToString(swagger);
      }
    };

    serviceRegistry.getSwaggerLoader().unregisterSwagger(appId, serviceName, schemaId);
    Assert.assertEquals(swagger,
        appManager.getOrCreateMicroserviceVersions(appId, serviceName)
            .getVersions().values().iterator().next()
            .getMicroserviceMeta()
            .findSchemaMeta(schemaId)
            .getSwagger());
  }

  @Test
  public void loadFromResource_sameApp_dirWithoutApp() throws IOException {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    mockLocalResource(swagger, String.format("microservices/%s/%s.yaml", serviceName, schemaId));

    serviceRegistry.getSwaggerLoader().unregisterSwagger(appId, serviceName, schemaId);
    Assert.assertEquals(swagger,
        appManager.getOrCreateMicroserviceVersions(appId, serviceName)
            .getVersions().values().iterator().next()
            .getMicroserviceMeta()
            .findSchemaMeta(schemaId)
            .getSwagger());
  }

  @Test
  public void loadFromResource_sameApp_dirWithApp() throws IOException {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    mockLocalResource(swagger, String.format("applications/%s/%s/%s.yaml", appId, serviceName, schemaId));

    serviceRegistry.getSwaggerLoader().unregisterSwagger(appId, serviceName, schemaId);
    Assert.assertEquals(swagger,
        appManager.getOrCreateMicroserviceVersions(appId, serviceName)
            .getVersions().values().iterator().next()
            .getMicroserviceMeta()
            .findSchemaMeta(schemaId)
            .getSwagger());
  }

  @Test
  public void loadFromResource_diffApp_dirWithoutApp() throws IOException {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    mockLocalResource(swagger, String.format("microservices/%s/%s.yaml", "ms3", schemaId));

    try (LogCollector logCollector = new LogCollector()) {
      appManager.getOrCreateMicroserviceVersions("other", "ms3");

      Assert.assertThat(logCollector.getThrowableMessages(),
          Matchers.contains(
              "no schema in local, and can not get schema from service center, appId=other, microserviceName=ms3, version=1.0, serviceId=003, schemaId=hello."));
    }
  }

  @Test
  public void loadFromResource_diffApp_dirWithApp() throws IOException {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    mockLocalResource(swagger, String.format("applications/%s/%s/%s.yaml", "other", "ms3", schemaId));

    Assert.assertEquals(swagger,
        appManager.getOrCreateMicroserviceVersions("other", "ms3")
            .getVersions().values().iterator().next()
            .getMicroserviceMeta()
            .findSchemaMeta(schemaId)
            .getSwagger());
  }

  private void mockLocalResource(Swagger swagger, String path) throws IOException {
    URL url = new MockUp<URL>() {

    }.getMockInstance();
    ClassLoader classLoader = new MockUp<ClassLoader>() {
      @Mock
      URL getResource(String name) {
        return path.equals(name) ? url : null;
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
      String toString(URL url) {
        return SwaggerUtils.swaggerToString(swagger);
      }
    };
  }
}
