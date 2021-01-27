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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.Json;

class ExceptionsTest {
  @Test
  void should_not_convert_invocation_exception() {
    InvocationException exception = Exceptions.genericConsumer("msg");

    assertThat(Exceptions.convert(null, exception, BAD_REQUEST))
        .isSameAs(exception);
  }

  @Test
  void should_convert_unknown_client_exception_to_invocation_exception() {
    IllegalStateException exception = new IllegalStateException("msg");

    InvocationException invocationException = Exceptions.convert(null, exception, BAD_REQUEST);

    assertThat(invocationException).hasCause(exception);
    assertThat(invocationException.getStatus()).isEqualTo(BAD_REQUEST);
    assertThat(invocationException.getErrorData()).isInstanceOf(CommonExceptionData.class);
    assertThat(Json.encode(invocationException.getErrorData()))
        .isEqualTo("{\"code\":\"SCB.00000000\",\"message\":\"msg\"}");
  }

  @Test
  void should_convert_unknown_server_exception_to_invocation_exception() {
    IllegalStateException exception = new IllegalStateException("msg");

    InvocationException invocationException = Exceptions.convert(null, exception, INTERNAL_SERVER_ERROR);

    assertThat(invocationException).hasCause(exception);
    assertThat(invocationException.getStatus()).isEqualTo(INTERNAL_SERVER_ERROR);
    assertThat(invocationException.getErrorData()).isInstanceOf(CommonExceptionData.class);
    assertThat(Json.encode(invocationException.getErrorData()))
        .isEqualTo("{\"code\":\"SCB.50000000\",\"message\":\"msg\"}");
  }

  static class ThrowExceptionWhenConvert implements ExceptionConverter<Throwable> {
    @Override
    public boolean canConvert(Throwable throwable) {
      return true;
    }

    @Override
    public InvocationException convert(@Nullable Invocation invocation, Throwable throwable, StatusType genericStatus) {
      throw new RuntimeExceptionWithoutStackTrace("mock exception when convert");
    }
  }

  @Test
  void should_protect_when_converter_throw_exception() {
    try (LogCollector logCollector = new LogCollector()) {
      Map<Class<?>, ExceptionConverter<Throwable>> cache = new HashMap<>();
      cache.put(RuntimeExceptionWithoutStackTrace.class, new ThrowExceptionWhenConvert());
      InvocationException exception = Exceptions
          .convert(null, new RuntimeExceptionWithoutStackTrace("exception need convert"), BAD_REQUEST, cache);

      assertThat(exception.getStatus())
          .isSameAs(INTERNAL_SERVER_ERROR);
      assertThat(exception.getErrorData().toString())
          .isEqualTo("CommonExceptionData{code='SCB.50000000', message='Internal Server Error', dynamic={}}");
      assertThat(logCollector.getLastEvents().getRenderedMessage().replace("\r\n", "\n"))
          .isEqualTo("BUG: ExceptionConverter.convert MUST not throw exception, please fix it.\n"
              + "original exception:org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace: exception need convert\n"
              + "converter exception:org.apache.servicecomb.foundation.test.scaffolding.exception.RuntimeExceptionWithoutStackTrace: mock exception when convert\n");
    }
  }
}