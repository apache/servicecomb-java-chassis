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
package org.apache.servicecomb.swagger.invocation.springmvc.response;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.converter.ConverterMgr;
import org.apache.servicecomb.swagger.invocation.response.ResponseMapperFactorys;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapperFactory;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

public class TestSpringmvcConsumerResponseMapperFactory {
  SpringmvcConsumerResponseMapperFactory factory = new SpringmvcConsumerResponseMapperFactory();

  ConverterMgr converterMgr = new ConverterMgr();

  ResponseMapperFactorys<ConsumerResponseMapper> factorys =
      new ResponseMapperFactorys<>(ConsumerResponseMapperFactory.class, converterMgr);

  public CompletableFuture<ResponseEntity<String[]>> asyncResponseEntityMethod() {
    return null;
  }

  public ResponseEntity<String[]> responseEntity() {
    return null;
  }

  public List<String> list() {
    return null;
  }

  @Test
  public void isMatch_true() {
    Method method = ReflectUtils.findMethod(this.getClass(), "responseEntity");
    Assert.assertTrue(factory.isMatch(null, method.getGenericReturnType()));
  }

  @Test
  public void isMatch_Parameterized_false() {
    Method method = ReflectUtils.findMethod(this.getClass(), "list");
    Assert.assertFalse(factory.isMatch(null, method.getGenericReturnType()));
  }

  @Test
  public void isMatch_false() {
    Assert.assertFalse(factory.isMatch(null, String.class));
  }

  @Test
  public void createResponseMapper() {
    Method responseEntityMethod = ReflectUtils.findMethod(this.getClass(), "responseEntity");
    Method listMethod = ReflectUtils.findMethod(this.getClass(), "list");

    ConsumerResponseMapper mapper = factory
        .createResponseMapper(factorys, listMethod.getGenericReturnType(), responseEntityMethod.getGenericReturnType());
    Assert.assertThat(mapper, Matchers.instanceOf(SpringmvcConsumerResponseMapper.class));

    Response response = Response.ok(Arrays.asList("a", "b"));
    @SuppressWarnings("unchecked")
    ResponseEntity<String[]> responseEntity = (ResponseEntity<String[]>) mapper.mapResponse(response);
    Assert.assertThat(responseEntity.getBody(), Matchers.arrayContaining("a", "b"));
  }

  @Test
  public void asyncResponseEntity() {
    Method responseEntityMethod = ReflectUtils.findMethod(this.getClass(), "asyncResponseEntityMethod");
    Method listMethod = ReflectUtils.findMethod(this.getClass(), "list");

    ConsumerResponseMapper mapper = factorys
        .createResponseMapper(listMethod.getGenericReturnType(), responseEntityMethod.getGenericReturnType());
    Assert.assertThat(mapper, Matchers.instanceOf(SpringmvcConsumerResponseMapper.class));

    Response response = Response.ok(Arrays.asList("a", "b"));
    @SuppressWarnings("unchecked")
    ResponseEntity<String[]> responseEntity = (ResponseEntity<String[]>) mapper.mapResponse(response);
    Assert.assertThat(responseEntity.getBody(), Matchers.arrayContaining("a", "b"));
  }
}
