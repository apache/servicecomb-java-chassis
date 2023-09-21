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

package org.apache.servicecomb.common.rest.codec.param;

import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import jakarta.servlet.http.HttpServletRequest;

public class TestQueryProcessorCreator {
  static Environment environment = Mockito.mock(Environment.class);

  @BeforeAll
  public static void beforeClass() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.emptyAsNull", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.ignoreDefaultValue", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.ignoreRequiredCheck", boolean.class, false))
        .thenReturn(false);
  }

  @Test
  public void testCreate() {
    ParamValueProcessorCreator creator =
        ParamValueProcessorCreatorManager.INSTANCE.findValue(QueryProcessorCreator.PARAMTYPE);
    Parameter parameter = new QueryParameter();
    parameter.setName("query");
    parameter.setSchema(new Schema());
    ParamValueProcessor processor = creator.create(null, parameter.getName(), parameter, String.class);

    Assertions.assertEquals(QueryProcessor.class, processor.getClass());

    String result = (String) processor.convertValue("Hello", TypeFactory.defaultInstance().constructType(String.class));
    Assertions.assertEquals("Hello", result);

    result = (String) processor.convertValue("", TypeFactory.defaultInstance().constructType(String.class));
    Assertions.assertEquals("", result);

    result = (String) processor.convertValue(null, TypeFactory.defaultInstance().constructType(String.class));
    Assertions.assertNull(result);
  }

  @SuppressWarnings("UnusedAssignment")
  @Test
  public void testCreateNullAsEmpty() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.emptyAsNull", boolean.class, false))
        .thenReturn(true);
    ParamValueProcessorCreator creator =
        ParamValueProcessorCreatorManager.INSTANCE.findValue(QueryProcessorCreator.PARAMTYPE);
    Parameter parameter = new QueryParameter();
    parameter.setName("query");
    parameter.setSchema(new Schema());

    ParamValueProcessor processor = creator.create(null, parameter.getName(), parameter, String.class);

    Assertions.assertEquals(QueryProcessor.class, processor.getClass());

    Mockito.when(request.getParameter("query")).thenReturn("Hello");
    String result = (String) processor.getValue(request);
    Assertions.assertEquals("Hello", result);

    Mockito.when(request.getParameter("query")).thenReturn("");
    result = (String) processor.getValue(request);
    Assertions.assertNull(result);

    Mockito.when(request.getParameter("query")).thenReturn(null);
    result = (String) processor.convertValue(null, TypeFactory.defaultInstance().constructType(String.class));
    result = (String) processor.getValue(request);
    Assertions.assertNull(result);
    ArchaiusUtils.resetConfig();
  }
}
