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

package org.apache.servicecomb.common.rest.codec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.common.rest.codec.param.ParamValueProcessor;
import org.apache.servicecomb.common.rest.codec.param.RestClientRequestImpl;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response.Status;

public class TestRestCodec {

  private static RestOperationMeta restOperation;

  private static final Map<String, String> header = new HashMap<>();

  private static RestClientRequest clientRequest = new RestClientRequestImpl(null, null, null) {
    public void putHeader(String name, String value) {
      header.put(name, value);
    }
  };

  private static List<RestParam> paramList = null;

  static Environment environment = Mockito.mock(Environment.class);

  @BeforeAll
  public static void beforeClass() {
    LegacyPropertyFactory.setEnvironment(environment);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.decodeAsObject", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.emptyAsNull", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.ignoreDefaultValue", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.query.ignoreRequiredCheck", boolean.class, false))
        .thenReturn(false);
    Mockito.when(environment.getProperty("servicecomb.rest.parameter.header.ignoreRequiredCheck", boolean.class, false))
        .thenReturn(false);

    Parameter hp = new HeaderParameter();
    hp.setName("header");
    hp.setSchema(new Schema());
    RestParam restParam = new RestParam(null, hp, int.class);

    restOperation = Mockito.mock(RestOperationMeta.class);
    paramList = new ArrayList<>();

    paramList.add(restParam);
    Mockito.when(restOperation.getParamList()).thenReturn(paramList);
    Mockito.when(restOperation.getParamByName("test")).thenReturn(restParam);
  }

  @AfterAll
  public static void afterClass() {
    restOperation = null;
    clientRequest = null;
    paramList.clear();
  }

  @Test
  public void testArgsToRest() {
    try {
      Map<String, Object> args = new HashMap<>();
      args.put("header", "abc");
      RestCodec.argsToRest(args, restOperation, clientRequest);
      Assertions.assertEquals("abc", header.get("header"));
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail();
    }
  }

  @Test
  public void testRestToArgs() throws Exception {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    RestOperationMeta restOperation = Mockito.mock(RestOperationMeta.class);
    RestParam restParam = Mockito.mock(RestParam.class);
    ParamValueProcessor processor = Mockito.mock(ParamValueProcessor.class);
    List<RestParam> params = new ArrayList<>();
    params.add(restParam);
    String s = "my";
    Mockito.when(restOperation.getParamList()).thenReturn(params);
    Mockito.when(restParam.getParamProcessor()).thenReturn(processor);
    Mockito.when(processor.getValue(request)).thenReturn(s);
    Mockito.when(restParam.getParamName()).thenReturn("test");

    Map<String, Object> xx = RestCodec.restToArgs(request, restOperation);
    Assertions.assertEquals(xx.get("test"), s);
  }

  @Test
  public void testRestToArgsException() throws Exception {
    ParamValueProcessor processor = Mockito.mock(ParamValueProcessor.class);
    Mockito.when(processor.getValue(Mockito.any())).thenThrow(new Exception("bad request parame"));

    RestOperationMeta restOperation = Mockito.mock(RestOperationMeta.class);
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(restParam.getParamProcessor()).thenReturn(processor);

    List<RestParam> params = new ArrayList<>();
    params.add(restParam);
    Mockito.when(restOperation.getParamList()).thenReturn(params);

    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(restOperation.getOperationMeta()).thenReturn(operationMeta);

    boolean success = false;
    try {
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      RestCodec.restToArgs(request, restOperation);
      success = true;
    } catch (InvocationException e) {
      Assertions.assertEquals(400, e.getStatusCode());
      Assertions.assertTrue(e.getMessage().contains("Parameter is not valid"));
    }
    Assertions.assertFalse(success);
  }

  @Test
  public void testRestToArgsInstanceException() throws Exception {
    InvocationException exception = new InvocationException(Status.BAD_REQUEST, "Parameter is not valid.");
    ParamValueProcessor processor = Mockito.mock(ParamValueProcessor.class);
    Mockito.when(processor.getValue(Mockito.any())).thenThrow(exception);

    RestOperationMeta restOperation = Mockito.mock(RestOperationMeta.class);
    RestParam restParam = Mockito.mock(RestParam.class);
    Mockito.when(restParam.getParamProcessor()).thenReturn(processor);

    List<RestParam> params = new ArrayList<>();
    params.add(restParam);
    Mockito.when(restOperation.getParamList()).thenReturn(params);

    OperationMeta operationMeta = Mockito.mock(OperationMeta.class);
    Mockito.when(restOperation.getOperationMeta()).thenReturn(operationMeta);

    boolean success = false;
    try {
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      RestCodec.restToArgs(request, restOperation);
      success = true;
    } catch (InvocationException e) {
      Assertions.assertEquals(e.getStatusCode(), Status.BAD_REQUEST.getStatusCode());
      Assertions.assertTrue(((CommonExceptionData) e.getErrorData()).getMessage()
          .contains("Parameter is not valid for operation"));
    }
    Assertions.assertFalse(success);
  }
}
