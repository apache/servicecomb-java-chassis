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
package org.apache.servicecomb.swagger.invocation.response;

import java.util.List;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.converter.Converter;
import org.apache.servicecomb.swagger.invocation.converter.ConverterMgr;
import org.apache.servicecomb.swagger.invocation.converter.impl.ConverterSame;
import org.apache.servicecomb.swagger.invocation.response.consumer.CompletableFutureConsumerResponseMapperFactory;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapperFactory;
import org.apache.servicecomb.swagger.invocation.response.consumer.CseResponseConsumerResponseMapperFactory;
import org.apache.servicecomb.swagger.invocation.response.consumer.DefaultConsumerResponseMapper;
import org.apache.servicecomb.swagger.invocation.response.consumer.DefaultConsumerResponseMapperFactory;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Deencapsulation;

public class TestResponseMapperFactorys {
  ResponseMapperFactorys<ConsumerResponseMapper> consumerResponseMapperFactorys =
      new ResponseMapperFactorys<>(ConsumerResponseMapperFactory.class);

  List<ResponseMapperFactory<ConsumerResponseMapper>> factorys =
      Deencapsulation.getField(consumerResponseMapperFactorys, "factorys");

  ConverterMgr converterMgr = new ConverterMgr();

  @Before
  public void setup() {
    consumerResponseMapperFactorys.setConverterMgr(converterMgr);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void construct() {
    Assert.assertThat(factorys,
        Matchers.contains(Matchers.instanceOf(CseResponseConsumerResponseMapperFactory.class),
            Matchers.instanceOf(CompletableFutureConsumerResponseMapperFactory.class),
            Matchers.instanceOf(DefaultConsumerResponseMapperFactory.class)));
  }

  @Test
  public void setConverterMgr() {
    Assert.assertSame(converterMgr, Deencapsulation.getField(factorys.get(2), "converterMgr"));
  }

  @Test
  public void createResponseMapper_default() {
    ConsumerResponseMapper mapper = consumerResponseMapperFactorys.createResponseMapper(String.class, String.class);
    Assert.assertThat(mapper, Matchers.instanceOf(DefaultConsumerResponseMapper.class));

    Converter converter = Deencapsulation.getField(mapper, "converter");
    Assert.assertSame(ConverterSame.getInstance(), converter);
  }

  @Test
  public void createResponseMapper_cseResponse() {
    ConsumerResponseMapper mapper = consumerResponseMapperFactorys.createResponseMapper(String.class, Response.class);

    Response response = Response.ok(null);
    Object result = mapper.mapResponse(response);
    Assert.assertSame(response, result);
  }
}
