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

import java.util.concurrent.TimeoutException;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionConverter;
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;

public class ServiceCombExceptionConverter implements ExceptionConverter<ServiceCombException> {
  public static final int ORDER = Byte.MAX_VALUE;

  private TimeoutExceptionConverter timeoutExceptionConverter = new TimeoutExceptionConverter();

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public boolean canConvert(Throwable throwable) {
    return throwable instanceof ServiceCombException;
  }

  @Override
  public InvocationException convert(Invocation invocation, ServiceCombException throwable,
      StatusType genericStatus) {
    if (throwable.getCause() instanceof TimeoutException) {
      return timeoutExceptionConverter.convert(invocation, (TimeoutException) throwable.getCause(), genericStatus);
    }

    return new InvocationException(Status.INTERNAL_SERVER_ERROR,
        ExceptionConverter.getGenericCode(genericStatus), throwable.getMessage(),
        throwable);
  }
}
