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

package org.apache.servicecomb.common.rest.definition;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;
import mockit.Expectations;
import mockit.Mocked;

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
  public void testCreateProduceProcessorsNull() {
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR,
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR, operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR,
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findValue(produce);
      Assert.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsEmpty() {
    operationMeta.produces = Arrays.asList();
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR,
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR, operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR,
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findValue(produce);
      Assert.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsNormal() {
    operationMeta.produces = Arrays.asList(MediaType.APPLICATION_JSON);
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR,
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR, operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR,
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assert.assertSame(ProduceProcessorManager.JSON_PROCESSOR,
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assert.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsNotSupported() {
    operationMeta.produces = Arrays.asList("notSupport");
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR,
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR, operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.DEFAULT_PROCESSOR,
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assert.assertSame(ProduceProcessorManager.JSON_PROCESSOR,
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assert.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsTextAndWildcard() {
    operationMeta.produces = Arrays.asList(MediaType.TEXT_PLAIN);
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.PLAIN_PROCESSOR,
        operationMeta.ensureFindProduceProcessor(MediaType.WILDCARD));
    Assert.assertSame(ProduceProcessorManager.PLAIN_PROCESSOR,
        operationMeta.ensureFindProduceProcessor(MediaType.TEXT_PLAIN));
    Assert.assertNull(operationMeta.ensureFindProduceProcessor(MediaType.APPLICATION_JSON));
    Assert.assertSame(ProduceProcessorManager.PLAIN_PROCESSOR,
        operationMeta.ensureFindProduceProcessor(
            MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML + "," + MediaType.WILDCARD));
  }

  @Test
  public void testEnsureFindProduceProcessorRequest(@Mocked HttpServletRequestEx requestEx) {
    new Expectations() {
      {
        requestEx.getHeader(HttpHeaders.ACCEPT);
        result = null;
      }
    };
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.JSON_PROCESSOR, operationMeta.ensureFindProduceProcessor(requestEx));
  }

  @Test
  public void testEnsureFindProduceProcessorAcceptFound() {
    operationMeta.produces = Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN);
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.JSON_PROCESSOR,
        operationMeta.ensureFindProduceProcessor("text/plain;q=0.7;charset=utf-8,application/json;q=0.8"));
  }

  @Test
  public void testEnsureFindProduceProcessorAcceptNotFound() {
    operationMeta.produces = Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN);
    operationMeta.createProduceProcessors();

    Assert.assertNull(operationMeta.ensureFindProduceProcessor("notSupport"));
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

  @Test
  public void testFormDataFlagTrue() {
    when(meta.getMethod()).thenReturn(ReflectUtils.findMethod(SomeRestController.class, "form"));
    when(meta.getSwaggerOperation()).thenReturn(operation);
    List<Parameter> params = Arrays.asList(new FormParameter());
    when(operation.getParameters()).thenReturn(params);

    operationMeta.init(meta);

    assertThat(operationMeta.isFormData(), is(true));
  }

  @Test
  public void testFormDataFlagFalse() {
    when(meta.getMethod()).thenReturn(ReflectUtils.findMethod(SomeRestController.class, "form"));
    when(meta.getSwaggerOperation()).thenReturn(operation);
    List<Parameter> params = Arrays.asList(new QueryParameter());
    when(operation.getParameters()).thenReturn(params);

    operationMeta.init(meta);

    assertThat(operationMeta.isFormData(), is(false));
  }

  private static class SomeRestController {
    @SuppressWarnings("unused")
    public String sayHi() {
      return "Hi";
    }

    @SuppressWarnings("unused")
    public String form(String param) {
      return "";
    }
  }
}
