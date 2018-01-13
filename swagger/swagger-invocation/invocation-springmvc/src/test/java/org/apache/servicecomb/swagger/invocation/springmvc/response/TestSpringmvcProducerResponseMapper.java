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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.foundation.common.http.HttpStatus;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.response.producer.ProducerResponseMapper;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestSpringmvcProducerResponseMapper {
  @Mocked
  ProducerResponseMapper realMapper;

  SpringmvcProducerResponseMapper mapper;

  String[] arrResult = new String[] {"a", "b"};

  @Before
  public void setup() {
    mapper = new SpringmvcProducerResponseMapper(realMapper);

    new MockUp<ProducerResponseMapper>(realMapper) {
      @Mock
      Response mapResponse(StatusType status, Object response) {
        if (HttpStatus.isSuccess(status.getStatusCode())) {
          return Response.ok(Arrays.asList(arrResult));
        }

        return null;
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Test
  public void mapResponse_withoutHeader_sucess() {
    ResponseEntity<String[]> responseEntity =
        new ResponseEntity<>(arrResult, org.springframework.http.HttpStatus.OK);
    Response response = mapper.mapResponse(null, responseEntity);
    Assert.assertThat((List<String>) response.getResult(), Matchers.contains("a", "b"));
    Assert.assertEquals(Status.OK, response.getStatus());
  }

  @Test
  public void mapResponse_withoutHeader_fail() {
    ResponseEntity<String[]> responseEntity =
        new ResponseEntity<>(arrResult, org.springframework.http.HttpStatus.BAD_REQUEST);
    Response response = mapper.mapResponse(null, responseEntity);
    Assert.assertSame(arrResult, response.getResult());
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus().getStatusCode());
  }

  @Test
  public void mapResponse_withHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("h", "v");

    ResponseEntity<String[]> responseEntity =
        new ResponseEntity<>(arrResult, headers, org.springframework.http.HttpStatus.OK);
    Response response = mapper.mapResponse(null, responseEntity);

    List<Object> hv = response.getHeaders().getHeader("h");
    Assert.assertThat(hv, Matchers.contains("v"));
  }
}
