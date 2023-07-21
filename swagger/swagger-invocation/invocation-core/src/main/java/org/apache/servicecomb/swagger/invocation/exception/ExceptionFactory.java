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
package org.apache.servicecomb.swagger.invocation.exception;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import jakarta.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.context.HttpStatus;

public final class ExceptionFactory {
  // cse内置的错误
  // 考虑到springmvc不支持自定义http错误码，所以只能使用“标准”错误码
  // 510是ws.rs中未定义，在springmvc中定义为not extended
  // 在我们的流程中不可能用到这个错误定义，所以将之转义为cse的provider内部错误
  //    private static final int PROVIDER_INNER_STATUS_CODE = 510;
  public static final int PRODUCER_INNER_STATUS_CODE = 590;

  public static final String PRODUCER_INNER_REASON_PHRASE = "Unexpected producer error, please check logs for details";

  public static final StatusType PRODUCER_INNER_STATUS =
      new HttpStatus(PRODUCER_INNER_STATUS_CODE, PRODUCER_INNER_REASON_PHRASE);

  // 420是ws.rs中未定义，在springmvc中定义为Method Failure
  // 在我们的流程中不可能用到这个错误定义，所以将之转义为cse的consumer内部错误
  //    private static final int CONSUMER_INNER_STATUS_CODE = 420;
  public static final int CONSUMER_INNER_STATUS_CODE = 490;

  public static final String CONSUMER_INNER_REASON_PHRASE = "Unexpected consumer error, please check logs for details";

  private static final ExceptionToProducerResponseConverters exceptionToProducerResponseConverters = new ExceptionToProducerResponseConverters();

  public static final StatusType CONSUMER_INNER_STATUS =
      new HttpStatus(CONSUMER_INNER_STATUS_CODE, CONSUMER_INNER_REASON_PHRASE);

  private ExceptionFactory() {
  }

  public static InvocationException create(StatusType status,
      Object exceptionOrErrorData) {
    if (InvocationException.class.isInstance(exceptionOrErrorData)) {
      return (InvocationException) exceptionOrErrorData;
    }

    return doCreate(status, exceptionOrErrorData);
  }

  public static InvocationException createConsumerException(Object errorData) {
    return create(CONSUMER_INNER_STATUS, errorData);
  }

  public static InvocationException createProducerException(Object errorData) {
    return create(PRODUCER_INNER_STATUS, errorData);
  }

  private static InvocationException doCreate(StatusType status,
                                              Object errorData) {
    return new InvocationException(status, errorData);
  }

  private static InvocationException doCreate(int statusCode, String reasonPhrase, CommonExceptionData data,
                                              Throwable e) {
    return new InvocationException(statusCode, reasonPhrase, data, e);
  }

  public static InvocationException convertConsumerException(Throwable e) {
    return convertException(CONSUMER_INNER_STATUS_CODE,
        CONSUMER_INNER_REASON_PHRASE,
        e,
        CONSUMER_INNER_REASON_PHRASE);
  }

  public static InvocationException convertConsumerException(Throwable e, String errorMsg) {
    return convertException(CONSUMER_INNER_STATUS_CODE,
        CONSUMER_INNER_REASON_PHRASE,
        e,
        errorMsg);
  }

  public static InvocationException convertProducerException(Throwable e) {
    return convertException(PRODUCER_INNER_STATUS_CODE,
        PRODUCER_INNER_REASON_PHRASE,
        e,
        PRODUCER_INNER_REASON_PHRASE);
  }

  public static InvocationException convertProducerException(Throwable e, String errorMsg) {
    return convertException(PRODUCER_INNER_STATUS_CODE,
        PRODUCER_INNER_REASON_PHRASE,
        e,
        errorMsg);
  }

  // 如果e中取不出可以直接返回的InvocationException
  // 则需要创建新的InvocationException实例，此时不允许使用e.getMessage，因为可能会涉及关键信息被传给远端
  // 新创建的InvocationException，会使用errorMsg来构建CommonExceptionData
  private static InvocationException convertException(int statusCode, String reasonPhrase, Throwable e,
                                                      String errorMsg) {
    e = unwrap(e);

    if (e instanceof InvocationException) {
      return (InvocationException) e;
    }

    CommonExceptionData data = new CommonExceptionData(errorMsg);
    return doCreate(statusCode, reasonPhrase, data, e);
  }

  public static Response convertExceptionToResponse(SwaggerInvocation swaggerInvocation, Throwable e) {
    return exceptionToProducerResponseConverters.convertExceptionToResponse(swaggerInvocation, e);
  }

  public static Throwable unwrapIncludeInvocationException(Throwable throwable) {
    throwable = unwrap(throwable);
    if (throwable instanceof InvocationException) {
      throwable = throwable.getCause();
    }
    return throwable;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Throwable> T unwrap(Throwable throwable) {
    if (throwable instanceof InvocationTargetException) {
      throwable = ((InvocationTargetException) throwable).getTargetException();
    }
    if (throwable instanceof CompletionException) {
      throwable = throwable.getCause();
    }
    if (throwable instanceof ExecutionException) {
      throwable = throwable.getCause();
    }
    return (T) throwable;
  }
}
