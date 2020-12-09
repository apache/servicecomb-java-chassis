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

import javax.annotation.Nullable;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionConverter;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *   Only very few exceptions carry sensitive data
 *   If discard all exception messages for these very few exceptions, it will cause difficult to locate problems
 *
 *   so default to remain the messages, and log the exception
 *
 *   if want to change these:
 *   1. customize a new converter
 *   2. disabled log for this class by log configuration
 * </pre>
 */
public class DefaultExceptionConverter implements ExceptionConverter<Throwable> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionConverter.class);

  public static final int ORDER = Integer.MAX_VALUE;

  @Override
  public int getOrder() {
    return ORDER;
  }

  @Override
  public boolean canConvert(Throwable throwable) {
    return true;
  }

  @Override
  public InvocationException convert(@Nullable Invocation invocation, Throwable throwable, StatusType genericStatus) {
    String msg = throwable.getMessage();
    if (msg == null) {
      msg = "Unexpected exception when processing.";
    }

    LOGGER.error("convert unknown exception({}) to InvocationException, message={}.",
        throwable.getClass().getName(), msg);
    return new InvocationException(genericStatus, ExceptionConverter.getGenericCode(genericStatus),
        msg, throwable);
  }
}
