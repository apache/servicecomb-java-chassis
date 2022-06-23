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

package org.apache.servicecomb.core.governance;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.governance.handler.ext.AbstractCircuitBreakerExtension;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.ExceptionFactory;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;

@Component
public class ServiceCombCircuitBreakerExtension extends AbstractCircuitBreakerExtension {
  @Override
  protected String extractStatusCode(Object result) {
    if (!(result instanceof Response)) {
      return null;
    }
    Response resp = (Response) result;
    if (resp.isFailed()) {
      if (resp.getResult() instanceof InvocationException) {
        InvocationException e = resp.getResult();
        return String.valueOf(e.getStatusCode());
      }
    }
    return String.valueOf(resp.getStatusCode());
  }

  @Override
  public boolean isFailedResult(Throwable e) {
    if (e instanceof InvocationException) {
      InvocationException invocationException = (InvocationException) e;
      if (invocationException.getStatusCode() == Status.SERVICE_UNAVAILABLE.getStatusCode() ||
          invocationException.getStatusCode() == Status.BAD_GATEWAY.getStatusCode() ||
          invocationException.getStatusCode() == ExceptionFactory.PRODUCER_INNER_STATUS_CODE) {
        return true;
      }
    }
    return super.isFailedResult(e);
  }
}
