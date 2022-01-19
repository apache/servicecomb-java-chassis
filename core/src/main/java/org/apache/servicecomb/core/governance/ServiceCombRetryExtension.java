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

import org.apache.servicecomb.governance.handler.ext.AbstractRetryExtension;
import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.stereotype.Component;

@Component
public class ServiceCombRetryExtension extends AbstractRetryExtension {
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
  public boolean isRetry(Throwable e) {
    if (e instanceof InvocationException && ((InvocationException) e).getStatusCode() == Status.SERVICE_UNAVAILABLE
        .getStatusCode()) {
      return true;
    }
    return super.isRetry(e);
  }
}
