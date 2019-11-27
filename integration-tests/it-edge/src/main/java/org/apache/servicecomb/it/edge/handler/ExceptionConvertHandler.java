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

package org.apache.servicecomb.it.edge.handler;

import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.it.edge.converter.CustomException;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

public class ExceptionConvertHandler implements Handler {
  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    invocation.next(response -> {
      if (response.isFailed()) {
        Throwable e = response.getResult();
        if (e instanceof TimeoutException || e.getCause() instanceof TimeoutException) {
          CustomException customException = new CustomException("change the response", 777);
          InvocationException stt = new InvocationException(Status.EXPECTATION_FAILED, customException);
          response.setResult(stt);
          response.setStatus(stt.getStatus());
        }
      }
      asyncResp.complete(response);
    });
  }
}
