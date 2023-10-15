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

package org.apache.servicecomb.core.exception;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.apache.servicecomb.core.exception.ExceptionCodes.GENERIC_CLIENT;
import static org.apache.servicecomb.core.exception.ExceptionCodes.GENERIC_SERVER;
import static org.apache.servicecomb.swagger.invocation.InvocationType.CONSUMER;

import java.util.Comparator;
import java.util.List;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.ws.rs.core.Response.StatusType;

public class Exceptions {
  private static ExceptionProcessor processor = new DefaultExceptionProcessor();

  @Autowired(required = false)
  public void setProcessor(List<ExceptionProcessor> processors) {
    // never be null, "orElse" just to avoid compile warning
    processor = processors.stream()
        .min(Comparator.comparingInt(ExceptionProcessor::getOrder))
        .orElse(new DefaultExceptionProcessor());
  }

  public static Throwable unwrapIncludeInvocationException(Throwable throwable) {
    return ExceptionFactory.unwrapIncludeInvocationException(throwable);
  }

  public static <T extends Throwable> T unwrap(Throwable throwable) {
    return ExceptionFactory.unwrap(throwable);
  }

  public static InvocationException create(StatusType status, Object errorData) {
    return new InvocationException(status, errorData);
  }

  public static InvocationException create(StatusType status, String code, String msg) {
    return new InvocationException(status, code, msg);
  }

  private static InvocationException create(StatusType status, String code, String msg, Throwable cause) {
    return new InvocationException(status, code, msg, cause);
  }

  public static InvocationException consumer(String code, String msg) {
    return create(BAD_REQUEST, code, msg);
  }

  public static InvocationException consumer(String code, String msg, Throwable cause) {
    if (cause instanceof InvocationException) {
      return (InvocationException) cause;
    }
    return create(BAD_REQUEST, code, msg, cause);
  }

  public static InvocationException genericConsumer(String msg) {
    return consumer(GENERIC_CLIENT, msg);
  }

  public static InvocationException genericConsumer(String msg, Throwable cause) {
    return consumer(GENERIC_CLIENT, msg, cause);
  }

  public static InvocationException producer(String code, String msg) {
    return create(INTERNAL_SERVER_ERROR, code, msg);
  }

  public static InvocationException producer(String code, String msg, Throwable cause) {
    return create(INTERNAL_SERVER_ERROR, code, msg, cause);
  }

  public static InvocationException genericProducer(String msg) {
    return producer(GENERIC_SERVER, msg);
  }

  public static InvocationException genericProducer(String msg, Throwable cause) {
    return producer(GENERIC_SERVER, msg, cause);
  }

  public static StatusType getGenericStatus(Invocation invocation) {
    return CONSUMER.equals(invocation.getInvocationType()) ? BAD_REQUEST : INTERNAL_SERVER_ERROR;
  }

  public static Response toConsumerResponse(Invocation invocation, Throwable throwable) {
    return processor.toConsumerResponse(invocation, throwable);
  }

  public static Response toProducerResponse(Invocation invocation, Throwable exception) {
    return processor.toProducerResponse(invocation, exception);
  }

  public static InvocationException convert(Invocation invocation, Throwable throwable) {
    return processor.convert(invocation, throwable);
  }

  public static InvocationException convert(Invocation invocation, Throwable throwable,
      StatusType genericStatus) {
    return processor.convert(invocation, throwable, genericStatus);
  }

  public static boolean isPrintInvocationStackTrace() {
    return processor.isPrintStackTrace();
  }
}
