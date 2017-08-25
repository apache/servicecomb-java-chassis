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

package io.servicecomb.common.rest.definition;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.core.definition.SchemaMeta;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;

public class TestRestOperationMeta {

  private final Swagger swagger = Mockito.mock(Swagger.class);

  private final SchemaMeta schemaMeta = Mockito.mock(SchemaMeta.class);

  private final Operation operation = Mockito.mock(Operation.class);

  private final OperationMeta meta = mock(OperationMeta.class);

  private final RestOperationMeta operationMeta = new RestOperationMeta();

  @Before
  public void setUp() throws Exception {
    when(meta.getSchemaMeta()).thenReturn(schemaMeta);
    when(meta.getSwaggerOperation()).thenReturn(operation);
    when(meta.getMethod()).thenReturn(SomeRestController.class.getMethod("sayHi"));

    when(schemaMeta.getSwagger()).thenReturn(swagger);
  }

  @Test
  public void testSplitAcceptTypes() {
    String types = "application/json;charset=utf-8,*/*;q=0.9";
    Assert.assertArrayEquals(new String[] {"application/json", "*/*"}, operationMeta.splitAcceptTypes(types));
  }

  @Test
  public void testContainSpecType() {
    Assert.assertTrue(operationMeta.containSpecType(new String[] {MediaType.WILDCARD}, MediaType.WILDCARD));
    Assert.assertFalse(
        operationMeta.containSpecType(new String[] {MediaType.APPLICATION_JSON}, MediaType.TEXT_PLAIN));
  }

  @Test
  public void testEnsureFindProduceProcessor() {
    operationMeta.setDefaultProcessor(ProduceProcessorManager.JSON_PROCESSOR);

    Assert.assertEquals(operationMeta.getDefaultProcessor(), operationMeta.ensureFindProduceProcessor(""));

    Assert.assertEquals(operationMeta.getDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor(MediaType.WILDCARD));

    operationMeta.ensureFindProduceProcessor("####");
    operationMeta.createProduceProcessors();
    String types = "application/json;charset=utf-8,text/plain;q=0.8";
    Assert.assertEquals(ProduceProcessorManager.JSON_PROCESSOR, operationMeta.ensureFindProduceProcessor(types));

    Assert.assertEquals(null, operationMeta.getParamByName("test"));
    Assert.assertEquals(null, operationMeta.getPathBuilder());
    Assert.assertEquals(null, operationMeta.findProduceProcessor("test"));
  }

  @Test
  public void generatesAbsolutePathWithRootBasePath() {
    when(swagger.getBasePath()).thenReturn("/");
    when(meta.getOperationPath()).thenReturn("/sayHi/");

    operationMeta.init(meta);

    assertThat(operationMeta.getAbsolutePath(), is("/sayHi/"));
  }

  @Test
  public void generatesAbsolutePathWithNonRootBasePath() {
    when(swagger.getBasePath()).thenReturn("/rest");
    when(meta.getOperationPath()).thenReturn("/sayHi");

    operationMeta.init(meta);

    assertThat(operationMeta.getAbsolutePath(), is("/rest/sayHi/"));
  }

  @Test
  public void generatesAbsolutePathWithNullPath() {
    when(swagger.getBasePath()).thenReturn(null);
    when(meta.getOperationPath()).thenReturn(null);

    operationMeta.init(meta);

    assertThat(operationMeta.getAbsolutePath(), is("/"));
  }

  @Test
  public void generatesAbsolutePathWithEmptyPath() {
    when(swagger.getBasePath()).thenReturn("");
    when(meta.getOperationPath()).thenReturn("");

    operationMeta.init(meta);

    assertThat(operationMeta.getAbsolutePath(), is("/"));
  }

  @Test
  public void consecutiveSlashesAreRemoved() {
    when(swagger.getBasePath()).thenReturn("//rest//");
    when(meta.getOperationPath()).thenReturn("//sayHi//");

    operationMeta.init(meta);

    assertThat(operationMeta.getAbsolutePath(), is("/rest/sayHi/"));
  }

  private static class SomeRestController {
    @SuppressWarnings("unused")
    public String sayHi() {
      return "Hi";
    }
  }
}
