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

import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Assert;
import org.junit.Test;

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
    Assert.assertEquals(true, response.isSuccessed());
    Assert.assertEquals(false, response.isFailed());
    Assert.assertEquals(1, (int) response.getResult());
    Assert.assertEquals(Status.ACCEPTED.getStatusCode(), response.getStatusCode());
    Assert.assertEquals(Status.ACCEPTED.getReasonPhrase(), response.getReasonPhrase());
    Assert.assertEquals(Status.ACCEPTED, response.getStatus());

    ar.success(2);
    Assert.assertEquals(2, (int) response.getResult());
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatusCode());

    Response r = Response.succResp(3);
    ar.complete(r);
    Assert.assertEquals(r, response);

    ar.consumerFail(new Error("abc"));
    CommonExceptionData data = (CommonExceptionData) ((InvocationException) response.getResult()).getErrorData();
    Assert.assertEquals("Cse Internal Bad Request", data.getMessage());
    Assert.assertEquals(ExceptionFactory.CONSUMER_INNER_STATUS_CODE, response.getStatusCode());

    ar.fail(InvocationType.CONSUMER, new Error("abc"));
    data = (CommonExceptionData) ((InvocationException) response.getResult()).getErrorData();
    Assert.assertEquals("Cse Internal Bad Request", data.getMessage());
    Assert.assertEquals(ExceptionFactory.CONSUMER_INNER_STATUS_CODE, response.getStatusCode());

    InvocationException consumerException = new InvocationException(300, "abc", "def");
    ar.consumerFail(consumerException);
    Assert.assertEquals("def", ((InvocationException) response.getResult()).getErrorData());
    Assert.assertEquals(300, response.getStatusCode());

    ar.fail(InvocationType.CONSUMER, consumerException);
    Assert.assertEquals("def", ((InvocationException) response.getResult()).getErrorData());
    Assert.assertEquals(300, response.getStatusCode());

    ar.producerFail(new Error("abc"));
    data = (CommonExceptionData) ((InvocationException) response.getResult()).getErrorData();
    Assert.assertEquals("Cse Internal Server Error", data.getMessage());
    Assert.assertEquals(ExceptionFactory.PRODUCER_INNER_STATUS_CODE, response.getStatusCode());

    ar.fail(InvocationType.PRODUCER, new Error("abc"));
    data = (CommonExceptionData) ((InvocationException) response.getResult()).getErrorData();
    Assert.assertEquals("Cse Internal Server Error", data.getMessage());
    Assert.assertEquals(ExceptionFactory.PRODUCER_INNER_STATUS_CODE, response.getStatusCode());

    InvocationException producerException = new InvocationException(500, "abc", "def");
    ar.producerFail(producerException);
    Assert.assertEquals("def", ((InvocationException) response.getResult()).getErrorData());
    Assert.assertEquals(500, response.getStatusCode());

    ar.fail(InvocationType.PRODUCER, producerException);
    Assert.assertEquals("def", ((InvocationException) response.getResult()).getErrorData());
    Assert.assertEquals(500, response.getStatusCode());
  }

  @Test
  public void test() {
    Response r = Response.create(200, "200", 2);
    Assert.assertEquals(200, r.getStatusCode());
    Assert.assertEquals(2, (int) r.getResult());
    Response r1 = r.build();
    Assert.assertEquals(r, r1);

    r = Response.create(300, "300", 3);
    Assert.assertEquals(300, r.getStatusCode());
    Assert.assertEquals("300", r.getReasonPhrase());
    Assert.assertEquals(3, ((InvocationException) r.getResult()).getErrorData());

    r = Response.createSuccess(Status.OK, 2);
    Assert.assertEquals(200, r.getStatusCode());
    Assert.assertEquals(2, (int) r.getResult());

    r = Response.success(2, Status.OK);
    Assert.assertEquals(200, r.getStatusCode());
    Assert.assertEquals(2, (int) r.getResult());

    r = Response.createFail(InvocationType.CONSUMER, "abc");
    Assert.assertEquals("CommonExceptionData [message=abc]",
        ((InvocationException) r.getResult()).getErrorData().toString());
    Assert.assertEquals(490, r.getStatusCode());

    r = Response.createFail(InvocationType.PRODUCER, "def");
    Assert.assertEquals("CommonExceptionData [message=def]",
        ((InvocationException) r.getResult()).getErrorData().toString());
    Assert.assertEquals(590, r.getStatusCode());
  }
}
