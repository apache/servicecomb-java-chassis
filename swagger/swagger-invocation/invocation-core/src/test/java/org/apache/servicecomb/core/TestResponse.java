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
package org.apache.servicecomb.core;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class TestResponse {
  Response response;

  AsyncResponse ar = new AsyncResponse() {
    @Override
    public void handle(Response resp) {
      response = resp;
    }
  };

  @Test
  public void testAr() {
    ar.success(Status.ACCEPTED, 1);
    Assertions.assertTrue(response.isSucceed());
    Assertions.assertFalse(response.isFailed());
    Assertions.assertEquals(1, (int) response.getResult());
    Assertions.assertEquals(Status.ACCEPTED.getStatusCode(), response.getStatusCode());
    Assertions.assertEquals(Status.ACCEPTED.getReasonPhrase(), response.getReasonPhrase());
    Assertions.assertEquals(Status.ACCEPTED, response.getStatus());

    ar.success(2);
    Assertions.assertEquals(2, (int) response.getResult());
    Assertions.assertEquals(Status.OK.getStatusCode(), response.getStatusCode());

    Response r = Response.succResp(3);
    ar.complete(r);
    Assertions.assertEquals(r, response);

    ar.consumerFail(new RuntimeExceptionWithoutStackTrace("abc"));
    CommonExceptionData data = (CommonExceptionData) ((InvocationException) response.getResult()).getErrorData();
    Assertions.assertEquals("Unexpected consumer error, please check logs for details", data.getMessage());
    Assertions.assertEquals(ExceptionFactory.CONSUMER_INNER_STATUS_CODE, response.getStatusCode());

    ar.fail(InvocationType.CONSUMER, new RuntimeExceptionWithoutStackTrace("abc"));
    data = (CommonExceptionData) ((InvocationException) response.getResult()).getErrorData();
    Assertions.assertEquals("Unexpected consumer error, please check logs for details", data.getMessage());
    Assertions.assertEquals(ExceptionFactory.CONSUMER_INNER_STATUS_CODE, response.getStatusCode());

    InvocationException consumerException = new InvocationException(300, "abc", "def");
    ar.consumerFail(consumerException);
    Assertions.assertEquals("def", ((InvocationException) response.getResult()).getErrorData());
    Assertions.assertEquals(300, response.getStatusCode());

    ar.fail(InvocationType.CONSUMER, consumerException);
    Assertions.assertEquals("def", ((InvocationException) response.getResult()).getErrorData());
    Assertions.assertEquals(300, response.getStatusCode());

    ar.producerFail(new RuntimeExceptionWithoutStackTrace("abc"));
    data = (CommonExceptionData) ((InvocationException) response.getResult()).getErrorData();
    Assertions.assertEquals("Unexpected producer error, please check logs for details", data.getMessage());
    Assertions.assertEquals(ExceptionFactory.PRODUCER_INNER_STATUS_CODE, response.getStatusCode());

    ar.fail(InvocationType.PRODUCER, new RuntimeExceptionWithoutStackTrace("abc"));
    data = (CommonExceptionData) ((InvocationException) response.getResult()).getErrorData();
    Assertions.assertEquals("Unexpected producer error, please check logs for details", data.getMessage());
    Assertions.assertEquals(ExceptionFactory.PRODUCER_INNER_STATUS_CODE, response.getStatusCode());

    InvocationException producerException = new InvocationException(500, "abc", "def");
    ar.producerFail(producerException);
    Assertions.assertEquals("def", ((InvocationException) response.getResult()).getErrorData());
    Assertions.assertEquals(500, response.getStatusCode());

    ar.fail(InvocationType.PRODUCER, producerException);
    Assertions.assertEquals("def", ((InvocationException) response.getResult()).getErrorData());
    Assertions.assertEquals(500, response.getStatusCode());
  }

  @Test
  public void test() {
    Response r = Response.create(200, "200", 2);
    Assertions.assertEquals(200, r.getStatusCode());
    Assertions.assertEquals(2, (int) r.getResult());
    Response r1 = r.build();
    Assertions.assertEquals(r, r1);

    r = Response.create(300, "300", 3);
    Assertions.assertEquals(300, r.getStatusCode());
    Assertions.assertEquals("300", r.getReasonPhrase());
    Assertions.assertEquals(3, ((InvocationException) r.getResult()).getErrorData());

    r = Response.createSuccess(Status.OK, 2);
    Assertions.assertEquals(200, r.getStatusCode());
    Assertions.assertEquals(2, (int) r.getResult());

    r = Response.success(2, Status.OK);
    Assertions.assertEquals(200, r.getStatusCode());
    Assertions.assertEquals(2, (int) r.getResult());

    r = Response.createFail(InvocationType.CONSUMER, "abc");
    Assertions.assertEquals("CommonExceptionData [message=abc]",
        ((InvocationException) r.getResult()).getErrorData().toString());
    Assertions.assertEquals(490, r.getStatusCode());

    r = Response.createFail(InvocationType.PRODUCER, "def");
    Assertions.assertEquals("CommonExceptionData [message=def]",
        ((InvocationException) r.getResult()).getErrorData().toString());
    Assertions.assertEquals(590, r.getStatusCode());
  }
}
