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

package org.apache.servicecomb.swagger.invocation.context;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

/**
 * 设置特定的Cse Context数据
 */
public class InvocationContext {
  private static HttpStatusManager statusMgr = new HttpStatusManager();

  protected StatusType httpStatus;

  // value只能是简单类型
  protected Map<String, String> context = new HashMap<>();

  public InvocationContext() {
    httpStatus = Status.OK;
  }

  public Map<String, String> getContext() {
    return context;
  }

  public void setContext(Map<String, String> context) {
    this.context = context;
  }

  public void addContext(String key, String value) {
    context.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getContext(String key) {
    return (T) context.get(key);
  }

  public void addContext(InvocationContext otherContext) {
    addContext(otherContext.getContext());
  }

  public void addContext(Map<String, String> otherContext) {
    if (otherContext == null) {
      return;
    }

    context.putAll(otherContext);
  }

  public StatusType getStatus() {
    return httpStatus;
  }

  public void setStatus(StatusType status) {
    this.httpStatus = status;
  }

  public void setStatus(int statusCode, String reason) {
    httpStatus = new HttpStatus(statusCode, reason);
  }

  public void setStatus(int statusCode) {
    this.httpStatus = statusMgr.getOrCreateByStatusCode(statusCode);
  }
}
