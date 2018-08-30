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

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

public class TestQueryProcessorCreator {
  @Test
  public void testCreate() {
    ParamValueProcessorCreator creator =
        ParamValueProcessorCreatorManager.INSTANCE.findValue(QueryProcessorCreator.PARAMTYPE);
    Parameter parameter = new QueryParameter();
    parameter.setName("query");

    ParamValueProcessor processor = creator.create(parameter, String.class);

    Assert.assertEquals(QueryProcessor.class, processor.getClass());

    String result = (String) processor.convertValue("Hello", TypeFactory.defaultInstance().constructType(String.class));
    Assert.assertEquals("Hello", result);

    result = (String) processor.convertValue("", TypeFactory.defaultInstance().constructType(String.class));
    Assert.assertEquals("", result);

    result = (String) processor.convertValue(null, TypeFactory.defaultInstance().constructType(String.class));
    Assert.assertEquals(null, result);
  }

  @Test
  public void testCreateNullAsEmpty() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    ArchaiusUtils.setProperty("servicecomb.rest.parameter.query.emptyAsNull", "true");
    ParamValueProcessorCreator creator =
        ParamValueProcessorCreatorManager.INSTANCE.findValue(QueryProcessorCreator.PARAMTYPE);
    Parameter parameter = new QueryParameter();
    parameter.setName("query");

    ParamValueProcessor processor = creator.create(parameter, String.class);

    Assert.assertEquals(QueryProcessor.class, processor.getClass());

    Mockito.when(request.getParameter("query")).thenReturn("Hello");
    String result = (String) processor.getValue(request);
    Assert.assertEquals("Hello", result);

    Mockito.when(request.getParameter("query")).thenReturn("");
    result = (String) (String) processor.getValue(request);
    Assert.assertEquals(null, result);

    Mockito.when(request.getParameter("query")).thenReturn(null);
    result = (String) processor.convertValue(null, TypeFactory.defaultInstance().constructType(String.class));
    result = (String) (String) processor.getValue(request);
    Assert.assertEquals(null, result);
  }
}
