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

package org.apache.servicecomb.common.rest.locator;

import java.util.Map;

import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import mockit.Mocked;

public class TestServicePathManager {
  private static class TestServicePathManagerSchemaImpl {
    @SuppressWarnings("unused")
    public void static1() {
    }

    @SuppressWarnings("unused")
    public void static2() {
    }

    @SuppressWarnings("unused")
    public void dynamic1() {
    }

    @SuppressWarnings("unused")
    public void dynamic2() {
    }
  }

  private ServicePathManager spm;

  @Mocked
  ApplicationContext applicationContext;

  @Before
  public void setup() {
    BeanUtils.setContext(applicationContext);

    MicroserviceMeta mm = new MicroserviceMeta("app:ms");
    Swagger swagger = UnitTestSwaggerUtils.generateSwagger(TestServicePathManagerSchemaImpl.class).getSwagger();
    Map<String, Path> paths = swagger.getPaths();

    swagger.setBasePath("");
    Path path = paths.remove("/static1");
    paths.put("/root/rest/static1", path);

    path = paths.remove("/dynamic1");
    paths.put("/dynamic1/{id}", path);

    path = paths.remove("/dynamic2");
    paths.put("/dynamic2/{id}", path);

    SchemaMeta schemaMeta = new SchemaMeta(swagger, mm, "sid");

    spm = new ServicePathManager(mm);
    spm.addSchema(schemaMeta);
    spm.sortPath();
  }

  @After
  public void teardown() {
    BeanUtils.setContext(null);
  }

  @Test
  public void testBuildProducerPathsNoPrefix() {
    System.clearProperty(Const.URL_PREFIX);

    spm.buildProducerPaths();
    Assert.assertSame(spm.producerPaths, spm.swaggerPaths);
  }

  @Test
  public void testBuildProducerPathsHasPrefix() {
    System.setProperty(Const.URL_PREFIX, "/root/rest");

    spm.buildProducerPaths();

    // all locate should be success
    spm.producerLocateOperation("/root/rest/static1/", "POST");
    spm.producerLocateOperation("/root/rest/static2/", "POST");
    spm.producerLocateOperation("/root/rest/dynamic1/1/", "POST");
    spm.producerLocateOperation("/root/rest/dynamic2/1/", "POST");

    System.clearProperty(Const.URL_PREFIX);
  }
}
