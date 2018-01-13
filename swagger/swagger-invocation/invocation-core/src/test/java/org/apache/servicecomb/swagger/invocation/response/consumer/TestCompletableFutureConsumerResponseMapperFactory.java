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
package org.apache.servicecomb.swagger.invocation.response.consumer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.converter.ConverterMgr;
import org.apache.servicecomb.swagger.invocation.response.ResponseMapperFactorys;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestCompletableFutureConsumerResponseMapperFactory {
  CompletableFutureConsumerResponseMapperFactory factory = new CompletableFutureConsumerResponseMapperFactory();

  ConverterMgr converterMgr = new ConverterMgr();

  ResponseMapperFactorys<ConsumerResponseMapper> factorys =
      new ResponseMapperFactorys<>(ConsumerResponseMapperFactory.class, converterMgr);

  public CompletableFuture<String[]> consumer() {
    return null;
  }

  public List<String> swagger() {
    return null;
  }

  @Test
  public void isMatch_true() {
    Method method = ReflectUtils.findMethod(this.getClass(), "consumer");
    Assert.assertTrue(factory.isMatch(null, method.getGenericReturnType()));
  }

  @Test
  public void isMatch_Parameterized_false() {
    Method method = ReflectUtils.findMethod(this.getClass(), "swagger");
    Assert.assertFalse(factory.isMatch(null, method.getGenericReturnType()));
  }

  @Test
  public void isMatch_false() {
    Assert.assertFalse(factory.isMatch(null, String.class));
  }

  @Test
  public void completableFuture() {
    Method consumerMethod = ReflectUtils.findMethod(this.getClass(), "consumer");
    Method swaggerMethod = ReflectUtils.findMethod(this.getClass(), "swagger");
    ConsumerResponseMapper mapper = factory
        .createResponseMapper(factorys, swaggerMethod.getGenericReturnType(), consumerMethod.getGenericReturnType());

    Response response = Response.ok(Arrays.asList("a", "b"));
    String[] arr = (String[]) mapper.mapResponse(response);
    Assert.assertThat(arr, Matchers.arrayContaining("a", "b"));
  }
}
