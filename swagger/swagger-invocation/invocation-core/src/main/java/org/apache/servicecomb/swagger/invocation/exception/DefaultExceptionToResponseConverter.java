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

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultExceptionToResponseConverter implements ExceptionToResponseConverter<Throwable> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionToResponseConverter.class);

  @Override
  public Class<Throwable> getExceptionClass() {
    // default logic, not bind to special class
    return null;
  }

  @Override
  public Response convert(SwaggerInvocation swaggerInvocation, Throwable e) {
    LOGGER.error("invoke failed, invocation={}", swaggerInvocation.getInvocationQualifiedName(), e);
    return Response.producerFailResp(e);
  }
}
