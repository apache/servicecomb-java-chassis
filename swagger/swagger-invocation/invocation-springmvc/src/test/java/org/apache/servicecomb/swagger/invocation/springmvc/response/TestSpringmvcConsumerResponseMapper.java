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

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapper;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import mockit.Expectations;
import mockit.Mocked;

public class TestSpringmvcConsumerResponseMapper {
  @Mocked
  ConsumerResponseMapper realMapper;

  SpringmvcConsumerResponseMapper mapper;

  Response response = Response.ok("1");

  @Before
  public void setup() {
    mapper = new SpringmvcConsumerResponseMapper(realMapper);

    new Expectations() {
      {
        realMapper.mapResponse(response);
        result = 1;
      }
    };
  }

  @Test
  public void mapResponse_withoutHeader() {
    @SuppressWarnings("unchecked")
    ResponseEntity<Integer> responseEntity = (ResponseEntity<Integer>) mapper.mapResponse(response);
    Assert.assertEquals((Integer) 1, responseEntity.getBody());
    Assert.assertEquals(Status.OK.getStatusCode(), responseEntity.getStatusCodeValue());
  }

  @Test
  public void mapResponse_withHeader() {
    response.getHeaders().addHeader("h", "v");

    @SuppressWarnings("unchecked")
    ResponseEntity<Integer> responseEntity = (ResponseEntity<Integer>) mapper.mapResponse(response);
    Assert.assertThat(responseEntity.getHeaders().get("h"), Matchers.contains("v"));
  }
}
