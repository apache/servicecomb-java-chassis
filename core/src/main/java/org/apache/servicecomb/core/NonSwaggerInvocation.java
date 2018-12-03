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

package org.apache.servicecomb.core;

import org.apache.servicecomb.swagger.invocation.AsyncResponse;

public class NonSwaggerInvocation extends Invocation {
  private String appId;

  private String microserviceName;

  private String versionRule;

  private Handler nextHandler;

  public NonSwaggerInvocation(String appId, String microserviceName, String versionRule, Handler nextHandler) {
    this.appId = appId;
    this.microserviceName = microserviceName;
    this.versionRule = versionRule;
    this.nextHandler = nextHandler;
  }

  @Override
  public String getSchemaId() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getOperationName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getInvocationQualifiedName() {
    return microserviceName;
  }

  @Override
  public String getConfigTransportName() {
    return Const.RESTFUL;
  }

  @Override
  public String getMicroserviceName() {
    return microserviceName;
  }

  @Override
  public String getAppId() {
    return appId;
  }

  @Override
  public String getMicroserviceVersionRule() {
    return versionRule;
  }

  @Override
  public void next(AsyncResponse asyncResp) throws Exception {
    nextHandler.handle(this, asyncResp);
  }
}
