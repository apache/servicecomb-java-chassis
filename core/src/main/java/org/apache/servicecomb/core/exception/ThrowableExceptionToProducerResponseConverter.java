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

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionToProducerResponseConverter;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

public class ThrowableExceptionToProducerResponseConverter implements ExceptionToProducerResponseConverter<Throwable> {
  @Override
  public Class<Throwable> getExceptionClass() {
    return Throwable.class;
  }

  @Override
  public int getOrder() {
    return 20000;
  }

  @Override
  public Response convert(SwaggerInvocation swaggerInvocation, Throwable e) {
    InvocationException invocationException = new InvocationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "",
        new CommonExceptionData("Unexpected exception when processing the request."), e);
    return Response.failResp(invocationException);
  }
}
