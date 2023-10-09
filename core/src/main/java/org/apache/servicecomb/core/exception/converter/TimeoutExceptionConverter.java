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

import static jakarta.ws.rs.core.Response.Status.REQUEST_TIMEOUT;

import java.util.concurrent.TimeoutException;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionConverter;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response.StatusType;

public class TimeoutExceptionConverter implements ExceptionConverter<TimeoutException> {
  private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutExceptionConverter.class);

  public static final int ORDER = Short.MAX_VALUE;

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public boolean canConvert(Throwable throwable) {
    return throwable instanceof TimeoutException;
  }

  @Override
  public InvocationException convert(Invocation invocation, TimeoutException throwable,
      StatusType genericStatus) {
    // throwable.getMessage:
    //   The timeout period of 30000ms has been exceeded while executing GET /xxx for server 1.1.1.1:8080
    // should not copy the message to invocationException to avoid leak server ip address
    LOGGER.info("Request timeout, Details: {}.", throwable.getMessage());

    return new InvocationException(REQUEST_TIMEOUT,
        ExceptionConverter.getGenericCode(genericStatus), "Request Timeout.",
        throwable);
  }
}
