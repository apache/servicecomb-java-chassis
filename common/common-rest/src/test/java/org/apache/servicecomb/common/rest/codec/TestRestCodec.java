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

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.codec.param.ParamValueProcessor;
import org.apache.servicecomb.common.rest.codec.param.RestClientRequestImpl;
import org.apache.servicecomb.common.rest.definition.RestOperationMeta;
import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import mockit.Expectations;
import mockit.Mocked;

public class TestRestCodec {

  private static RestOperationMeta restOperation;

  private static Map<String, String> header = new HashMap<>();

  private static RestClientRequest clientRequest = new RestClientRequestImpl(null, null, null) {
    public void putHeader(String name, String value) {
      header.put(name, value);
    }
  };

  private static List<RestParam> paramList = null;

  @BeforeClass
  public static void beforeClass() {
    restOperation = Mockito.mock(RestOperationMeta.class);
    //        clientRequest = Mockito.mock(RestClientRequest.class);
    paramList = new ArrayList<>();

    Parameter hp = new HeaderParameter();
    hp.setName("header");
    paramList.add(new RestParam(0, hp, int.class));
    when(restOperation.getParamList()).thenReturn(paramList);
  }

  @AfterClass
  public static void afterClass() {
    restOperation = null;
    clientRequest = null;
    paramList.clear();
  }

  @Test
  public void testArgsToRest() {
    try {
      RestCodec.argsToRest(new String[] {"abc"}, restOperation, clientRequest);
      Assert.assertEquals("abc", header.get("header"));
    } catch (Exception e) {
      Assert.assertTrue(false);
    }
  }

  @Test
  public void testRestToArgs(@Mocked HttpServletRequest request,
      @Mocked RestOperationMeta restOperation, @Mocked RestParam restParam,
      @Mocked ParamValueProcessor processer) throws Exception {
    List<RestParam> params = new ArrayList<>();
    params.add(restParam);
    String s = "my";

    new Expectations() {
      {
        restOperation.getParamList();
        result = params;
        restParam.getParamProcessor();
        result = processer;
        processer.getValue(request);
        result = s;
      }
    };

    Object[] xx = RestCodec.restToArgs(request, restOperation);
    Assert.assertEquals(xx[0], s);
  }

  @Test
  public void testRestToArgsExcetpion(@Mocked HttpServletRequest request,
      @Mocked RestOperationMeta restOperation, @Mocked RestParam restParam,
      @Mocked ParamValueProcessor processer) throws Exception {
    List<RestParam> params = new ArrayList<>();
    params.add(restParam);

    new Expectations() {
      {
        restOperation.getParamList();
        result = params;
        restParam.getParamProcessor();
        result = processer;
        processer.getValue(request);
        result = new Exception("bad request parame");
      }
    };

    boolean success = false;
    try {
      RestCodec.restToArgs(request, restOperation);
      success = true;
    } catch (InvocationException e) {
      Assert.assertEquals(590, e.getStatusCode());
      Assert.assertEquals("Parameter is not valid.", ((CommonExceptionData) e.getErrorData()).getMessage());
    }
    Assert.assertEquals(success, false);
  }

  @Test
  public void testRestToArgsInstanceExcetpion(@Mocked HttpServletRequest request,
      @Mocked RestOperationMeta restOperation, @Mocked RestParam restParam,
      @Mocked ParamValueProcessor processer) throws Exception {
    List<RestParam> params = new ArrayList<>();
    params.add(restParam);
    InvocationException exception = new InvocationException(Status.BAD_REQUEST, "Parameter is not valid.");

    new Expectations() {
      {
        restOperation.getParamList();
        result = params;
        restParam.getParamProcessor();
        result = processer;
        processer.getValue(request);
        result = exception;
      }
    };

    boolean success = false;
    try {
      RestCodec.restToArgs(request, restOperation);
      success = true;
    } catch (InvocationException e) {
      Assert.assertEquals(e.getStatusCode(), Status.BAD_REQUEST.getStatusCode());
    }
    Assert.assertEquals(success, false);
  }
}
