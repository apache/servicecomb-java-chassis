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

package org.apache.servicecomb.common.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.common.rest.locator.ServicePathManager;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.pojo.PojoSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import io.swagger.models.Swagger;

public class TestRestEngineSchemaListener {
  private final SwaggerGeneratorContext context = new PojoSwaggerGeneratorContext();

  private static class TestRestEngineSchemaListenerSchemaImpl {
    @SuppressWarnings("unused")
    public int add(int x, int y) {
      return 0;
    }
  }

  @Test
  public void test() {
    BeanUtils.setContext(Mockito.mock(ApplicationContext.class));

    MicroserviceMeta mm = new MicroserviceMeta("app:ms");
    List<SchemaMeta> smList = new ArrayList<>();

    SwaggerGenerator generator = new SwaggerGenerator(context, TestRestEngineSchemaListenerSchemaImpl.class);
    Swagger swagger = generator.generate();
    swagger.setBasePath("");
    SchemaMeta sm1 = new SchemaMeta(swagger, mm, "sid1");
    smList.add(sm1);

    RestEngineSchemaListener listener = new RestEngineSchemaListener();
    SchemaMeta[] smArr = smList.toArray(new SchemaMeta[smList.size()]);
    listener.onSchemaLoaded(smArr);
    // 重复调用，不应该出异常
    listener.onSchemaLoaded(smArr);

    ServicePathManager spm = ServicePathManager.getServicePathManager(mm);
    Assert.assertEquals(mm, spm.getMicroserviceMeta());

    Assert.assertSame(sm1,
        spm.consumerLocateOperation("/add/", "POST").getOperation().getOperationMeta().getSchemaMeta());
  }
}
