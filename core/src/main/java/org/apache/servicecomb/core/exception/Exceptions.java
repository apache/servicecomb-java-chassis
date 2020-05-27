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

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.apache.servicecomb.core.exception.ExceptionCodes.GENERIC_CLIENT;
import static org.apache.servicecomb.core.exception.ExceptionCodes.GENERIC_SERVER;
import static org.apache.servicecomb.swagger.invocation.InvocationType.CONSUMER;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

public final class Exceptions {
  @SuppressWarnings("unchecked")
  private static final List<ExceptionConverter<Throwable>> CONVERTERS = SPIServiceUtils
      .getOrLoadSortedService(ExceptionConverter.class).stream()
      .map(converter -> (ExceptionConverter<Throwable>) converter)
      .collect(Collectors.toList());

  private static final Map<Class<?>, ExceptionConverter<Throwable>> CACHE = new ConcurrentHashMapEx<>();

  private Exceptions() {
  }

  public static Throwable unwrapIncludeInvocationException(Throwable throwable) {
    return ExceptionFactory.unwrapIncludeInvocationException(throwable);
  }

  public static Throwable unwrap(Throwable throwable) {
    return ExceptionFactory.unwrap(throwable);
  }

  public static InvocationException create(Status status, String code, String msg) {
    return new InvocationException(status, code, msg);
  }

  private static InvocationException create(Status status, String code, String msg, Throwable cause) {
    return new InvocationException(status, code, msg, cause);
  }

  public static InvocationException consumer(String code, String msg) {
    return create(BAD_REQUEST, code, msg);
  }

  public static InvocationException consumer(String code, String msg, Throwable cause) {
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

  public static StatusType getGenericStatus(@Nonnull Invocation invocation) {
    return CONSUMER.equals(invocation.getInvocationType()) ? BAD_REQUEST : INTERNAL_SERVER_ERROR;
  }

  public static Response exceptionToResponse(@Nullable Invocation invocation, Throwable exception,
      StatusType genericStatus) {
    InvocationException invocationException = Exceptions.convert(invocation, exception, genericStatus);
    return Response.status(invocationException.getStatus())
        .entity(invocationException.getErrorData());
  }

  public static InvocationException convert(@Nonnull Invocation invocation, Throwable throwable) {
    StatusType genericStatus = getGenericStatus(invocation);
    return convert(invocation, throwable, genericStatus);
  }

  public static InvocationException convert(@Nullable Invocation invocation, Throwable throwable,
      StatusType genericStatus) {
    Throwable unwrapped = unwrap(throwable);
    return CACHE.computeIfAbsent(unwrapped.getClass(), clazz -> findConverter(unwrapped))
        .convert(invocation, unwrapped, genericStatus);
  }

  private static ExceptionConverter<Throwable> findConverter(Throwable throwable) {
    for (ExceptionConverter<Throwable> converter : CONVERTERS) {
      if (converter.canConvert(throwable)) {
        return converter;
      }
    }

    throw new IllegalStateException("never happened: can not find converter for " + throwable.getClass().getName());
  }
}
