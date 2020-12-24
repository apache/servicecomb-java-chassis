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

package org.apache.servicecomb.core.exception.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.exception.Exceptions;
import org.apache.servicecomb.foundation.test.scaffolding.log.LogCollector;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.jupiter.api.Test;

class TimeoutExceptionConverterTest {
  private InvocationException convert() {
    TimeoutException timeoutException = new TimeoutException(
        "The timeout period of 30000ms has been exceeded while executing GET /xxx for server 1.1.1.1:8080");

    InvocationException exception = Exceptions.convert(null, timeoutException, Status.BAD_REQUEST);
    return exception;
  }

  @Test
  void should_not_leak_server_ip() {
    InvocationException exception = convert();

    assertThat(exception)
        .hasMessage(
            "InvocationException: code=408;msg=CommonExceptionData{code='SCB.00000000', message='Request Timeout.', dynamic={}}");
  }

  @Test
  void should_log_detail() {
    try (LogCollector logCollector = new LogCollector()) {
      convert();

      assertThat(logCollector.getLastEvents().getRenderedMessage())
          .isEqualTo(
              "Request timeout, Details: The timeout period of 30000ms has been exceeded while executing GET /xxx for server 1.1.1.1:8080.");
    }
  }
}