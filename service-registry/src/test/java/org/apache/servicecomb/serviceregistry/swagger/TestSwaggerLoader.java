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
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.apache.servicecomb.serviceregistry.TestRegistryBase;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.swagger.models.Swagger;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

public class TestSwaggerLoader extends TestRegistryBase {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void registerSwagger() {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    serviceRegistry.getSwaggerLoader().registerSwagger("default:ms2", schemaId, swagger);

    Microservice microservice = appManager.getOrCreateMicroserviceVersions(appId, serviceName)
        .getVersions().values().iterator().next().getMicroservice();
    Assert.assertSame(swagger, serviceRegistry.getSwaggerLoader().loadSwagger(microservice, schemaId));
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

    Microservice microservice = appManager.getOrCreateMicroserviceVersions(appId, serviceName)
        .getVersions().values().iterator().next().getMicroservice();
    Swagger loadedSwagger = serviceRegistry.getSwaggerLoader().loadSwagger(microservice, schemaId);
    Assert.assertNotSame(swagger, loadedSwagger);
    Assert.assertEquals(swagger, loadedSwagger);
  }

  @Test
  public void loadFromResource_sameApp_dirWithoutApp() throws IOException {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    mockLocalResource(swagger, String.format("microservices/%s/%s.yaml", serviceName, schemaId));

    serviceRegistry.getSwaggerLoader().unregisterSwagger(appId, serviceName, schemaId);

    Microservice microservice = appManager.getOrCreateMicroserviceVersions(appId, serviceName)
        .getVersions().values().iterator().next().getMicroservice();
    Swagger loadedSwagger = serviceRegistry.getSwaggerLoader().loadSwagger(microservice, schemaId);
    Assert.assertNotSame(swagger, loadedSwagger);
    Assert.assertEquals(swagger, loadedSwagger);
  }

  @Test
  public void loadFromResource_sameApp_dirWithApp() throws IOException {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    mockLocalResource(swagger, String.format("applications/%s/%s/%s.yaml", appId, serviceName, schemaId));

    serviceRegistry.getSwaggerLoader().unregisterSwagger(appId, serviceName, schemaId);

    Microservice microservice = appManager.getOrCreateMicroserviceVersions(appId, serviceName)
        .getVersions().values().iterator().next().getMicroservice();
    Swagger loadedSwagger = serviceRegistry.getSwaggerLoader().loadSwagger(microservice, schemaId);
    Assert.assertNotSame(swagger, loadedSwagger);
    Assert.assertEquals(swagger, loadedSwagger);
  }

  @Test
  public void loadFromResource_diffApp_dirWithoutApp() throws IOException {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    mockLocalResource(swagger, String.format("microservices/%s/%s.yaml", "ms3", schemaId));

    Microservice microservice = appManager.getOrCreateMicroserviceVersions("other", "ms3")
        .getVersions().values().iterator().next().getMicroservice();

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(
        "no schema in local, and can not get schema from service center, appId=other, microserviceName=ms3, version=1.0, serviceId=003, schemaId=hello.");
    serviceRegistry.getSwaggerLoader().loadSwagger(microservice, schemaId);
  }

  @Test
  public void loadFromResource_diffApp_dirWithApp() throws IOException {
    Swagger swagger = SwaggerGenerator.generate(Hello.class);
    mockLocalResource(swagger, String.format("applications/%s/%s/%s.yaml", "other", "ms3", schemaId));

    Microservice microservice = appManager.getOrCreateMicroserviceVersions("other", "ms3")
        .getVersions().values().iterator().next().getMicroservice();
    Swagger loadedSwagger = serviceRegistry.getSwaggerLoader().loadSwagger(microservice, schemaId);
    Assert.assertNotSame(swagger, loadedSwagger);
    Assert.assertEquals(swagger, loadedSwagger);
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
      String toString(URL url, Charset encoding) {
        return SwaggerUtils.swaggerToString(swagger);
      }
    };
  }
}
