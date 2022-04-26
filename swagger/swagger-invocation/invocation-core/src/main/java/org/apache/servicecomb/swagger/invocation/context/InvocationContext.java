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
 *  InvocationContext is used to pass data between microservices or in microservice different layer.
 */
public class InvocationContext {
  private static final HttpStatusManager statusMgr = new HttpStatusManager();

  protected StatusType httpStatus;

  protected final Map<String, String> context = new HashMap<>();

  protected final Map<String, Object> localContext = new HashMap<>();

  protected TransportContext transportContext;

  public InvocationContext() {
    httpStatus = Status.OK;
  }

  public Map<String, String> getContext() {
    return context;
  }

  public void setContext(Map<String, String> context) {
    this.context.clear();
    this.addContext(context);
  }

  public void addContext(String key, String value) {
    context.put(key, value);
  }

  public String getContext(String key) {
    return context.get(key);
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

  public void mergeContext(InvocationContext otherContext) {
    mergeContext(otherContext.getContext());
  }

  public void mergeContext(Map<String, String> otherContext) {
    if (otherContext == null) {
      return;
    }
    context.putAll(otherContext);
  }

  public Map<String, Object> getLocalContext() {
    return localContext;
  }

  public void setLocalContext(Map<String, Object> localContext) {
    this.localContext.clear();
    this.addLocalContext(localContext);
  }

  public void addLocalContext(String key, Object value) {
    localContext.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getLocalContext(String key) {
    return (T) localContext.get(key);
  }

  public void addLocalContext(Map<String, Object> otherContext) {
    if (otherContext == null) {
      return;
    }

    localContext.putAll(otherContext);
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

  @SuppressWarnings("unchecked")
  public <T extends TransportContext> T getTransportContext() {
    return (T) transportContext;
  }

  public void setTransportContext(TransportContext transportContext) {
    this.transportContext = transportContext;
  }
}
