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
package org.apache.servicecomb.demo.springmvc.server;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.exception.ExceptionConverter;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.StatusType;

public class ResponseOKExceptionConverter implements
    ExceptionConverter<ResponseOKException> {

  @Override
  public boolean canConvert(Throwable throwable) {
    return throwable instanceof ResponseOKException;
  }

  @Override
  public InvocationException convert(Invocation invocation, ResponseOKException throwable, StatusType genericStatus) {
    // This is for compatible usage. For best practise, any status code
    // should have only one type of response.
    return new InvocationException(Status.OK, new ResponseOKData("code-005", "error-005"));
  }
}
