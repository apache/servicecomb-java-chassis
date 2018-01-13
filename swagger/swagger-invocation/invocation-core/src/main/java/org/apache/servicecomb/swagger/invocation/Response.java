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
package org.apache.servicecomb.swagger.invocation;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.swagger.invocation.context.HttpStatus;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.apache.servicecomb.swagger.invocation.response.Headers;

/**
 * 用jaxrs的Response能表达所有概念
 * 但是那个是abstract类，在不引入jersey之类的库的情况下，我们需要补充的功能太多
 *
 * 所以，使用一个简单的归一化类
 */
public class Response {
  private StatusType status;

  private Headers headers = new Headers();

  // 失败场景中，result是Throwable
  private Object result;

  public boolean isSuccessed() {
    return HttpStatus.isSuccess(status);
  }

  public boolean isFailed() {
    return !isSuccessed();
  }

  public int getStatusCode() {
    return status.getStatusCode();
  }

  public String getReasonPhrase() {
    return status.getReasonPhrase();
  }

  public StatusType getStatus() {
    return status;
  }

  public void setStatus(StatusType status) {
    this.status = status;
  }

  public Headers getHeaders() {
    return headers;
  }

  public void setHeaders(Headers headers) {
    this.headers = headers;
  }

  @SuppressWarnings("unchecked")
  public <T> T getResult() {
    return (T) result;
  }

  public void setResult(Object result) {
    this.result = result;
  }

  // 如果是成功，body即是result
  // 如果是失败，body可能是InvocationException，也可能是InvocationException中的errorData
  public static Response create(int statusCode, String reasonPhrase, Object result) {
    StatusType status = new HttpStatus(statusCode, reasonPhrase);
    return create(status, result);
  }

  public static Response create(StatusType status, Object result) {
    Response response = Response.status(status);
    if (response.isFailed()) {
      result = ExceptionFactory.create(status, result);
    }
    return response.entity(result);
  }

  // 有的场景下，需要返回非200的，其他2xx状态码，所以需要支持指定
  public static Response createSuccess(StatusType status, Object result) {
    return Response.status(status).entity(result);
  }

  public static Response createSuccess(Object result) {
    return Response.status(Status.OK).entity(result);
  }

  public static Response createFail(InvocationException exception) {
    return Response.status(exception.getStatus()).entity(exception);
  }

  public static Response createFail(InvocationType invocationType, String errorMsg) {
    CommonExceptionData errorData = new CommonExceptionData(errorMsg);
    if (InvocationType.CONSUMER.equals(invocationType)) {
      return createFail(ExceptionFactory.createConsumerException(errorData));
    }

    return createFail(ExceptionFactory.createProducerException(errorData));
  }

  public static Response createFail(InvocationType invocationType, Throwable throwable) {
    if (InvocationType.CONSUMER.equals(invocationType)) {
      return createConsumerFail(throwable);
    }

    return createProducerFail(throwable);
  }

  public static Response createConsumerFail(Throwable throwable) {
    InvocationException exception = ExceptionFactory.convertConsumerException(throwable);
    return createFail(exception);
  }

  public static Response createProducerFail(Throwable throwable) {
    InvocationException exception = ExceptionFactory.convertProducerException(throwable);
    return createFail(exception);
  }

  // 兼容接口

  public static Response consumerFailResp(Throwable e) {
    return createConsumerFail(e);
  }

  public static Response producerFailResp(Throwable e) {
    return createProducerFail(e);
  }

  public static Response providerFailResp(Throwable e) {
    return createProducerFail(e);
  }

  public static Response success(Object result, StatusType status) {
    return createSuccess(status, result);
  }

  public static Response succResp(Object result) {
    return createSuccess(result);
  }

  public static Response failResp(InvocationException e) {
    return createFail(e);
  }

  public static Response failResp(InvocationType invocationType, Throwable e) {
    return createFail(invocationType, e);
  }

  // 下面是jaxrs Response的一些常见用法，照搬过来
  public Response entity(Object result) {
    setResult(result);
    return this;
  }

  public Response build() {
    return this;
  }

  public static Response status(StatusType status) {
    Response response = new Response();
    response.setStatus(status);
    return response;
  }

  public static Response ok(Object result) {
    Response response = new Response();
    response.setStatus(Status.OK);
    response.setResult(result);
    return response;
  }
}
