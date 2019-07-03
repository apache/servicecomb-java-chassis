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
import java.util.List;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.response.ResponseMapperFactorys;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapperFactory;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TestSpringmvcProducerResponseMapperFactory {
  SpringmvcProducerResponseMapperFactory factory = new SpringmvcProducerResponseMapperFactory();

  ResponseMapperFactorys<ProducerResponseMapper> factorys = new ResponseMapperFactorys<>(
      ProducerResponseMapperFactory.class);

  public ResponseEntity<String[]> responseEntity() {
    return null;
  }

  public List<String> list() {
    return null;
  }

  @Test
  public void isMatch_true() {
    Method method = ReflectUtils.findMethod(this.getClass(), "responseEntity");
    Assert.assertTrue(factory.isMatch(method.getGenericReturnType()));
  }

  @Test
  public void isMatch_Parameterized_false() {
    Method method = ReflectUtils.findMethod(this.getClass(), "list");
    Assert.assertFalse(factory.isMatch(method.getGenericReturnType()));
  }

  @Test
  public void isMatch_false() {
    Assert.assertFalse(factory.isMatch(String.class));
  }

  @Test
  public void createResponseMapper() {
    Method responseEntityMethod = ReflectUtils.findMethod(this.getClass(), "responseEntity");

    ProducerResponseMapper mapper = factory
        .createResponseMapper(factorys, responseEntityMethod.getGenericReturnType());
    Assert.assertThat(mapper, Matchers.instanceOf(SpringmvcProducerResponseMapper.class));

    ResponseEntity<String[]> responseEntity = new ResponseEntity<>(new String[] {"a", "b"}, HttpStatus.OK);
    Response response = mapper.mapResponse(null, responseEntity);
    Assert.assertThat(response.getResult(), Matchers.arrayContaining("a", "b"));
  }
}
