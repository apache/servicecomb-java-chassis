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
package org.apache.servicecomb.swagger.invocation.response.producer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.converter.ConverterMgr;
import org.apache.servicecomb.swagger.invocation.response.ResponseMapperFactorys;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestCompletableFutureProducerResponseMapperFactory {
  CompletableFutureProducerResponseMapperFactory factory = new CompletableFutureProducerResponseMapperFactory();

  ConverterMgr converterMgr = new ConverterMgr();

  ResponseMapperFactorys<ProducerResponseMapper> factorys =
      new ResponseMapperFactorys<>(ProducerResponseMapperFactory.class, converterMgr);

  public CompletableFuture<String[]> producer() {
    return null;
  }

  public List<String> swagger() {
    return null;
  }

  @Test
  public void isMatch_true() {
    Method method = ReflectUtils.findMethod(this.getClass(), "producer");
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
    Method producerMethod = ReflectUtils.findMethod(this.getClass(), "producer");
    Method swaggerMethod = ReflectUtils.findMethod(this.getClass(), "swagger");
    ProducerResponseMapper mapper = factory
        .createResponseMapper(factorys, swaggerMethod.getGenericReturnType(), producerMethod.getGenericReturnType());

    Response response = mapper.mapResponse(Status.OK, new String[] {"a", "b"});
    Assert.assertThat(response.getResult(), Matchers.contains("a", "b"));
  }
}
