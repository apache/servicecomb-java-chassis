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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response.StatusType;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.core.Ordered;

/**
 * will select the min order instance
 */
public interface ExceptionProcessor extends Ordered {
  @Override
  default int getOrder() {
    return 0;
  }

  boolean isPrintStackTrace();

  InvocationException convert(@Nonnull Invocation invocation, Throwable throwable);

  InvocationException convert(@Nullable Invocation invocation, Throwable throwable, StatusType genericStatus);

  boolean isIgnoreLog(@Nonnull Invocation invocation, @Nonnull InvocationException exception);

  Response toConsumerResponse(Invocation invocation, Throwable throwable);

  void logConsumerException(@Nonnull Invocation invocation, @Nonnull InvocationException exception);

  Response toProducerResponse(@Nullable Invocation invocation, Throwable exception);

  void logProducerException(@Nonnull Invocation invocation, @Nonnull InvocationException exception);
}
