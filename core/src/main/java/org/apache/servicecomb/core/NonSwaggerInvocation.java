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

public class NonSwaggerInvocation extends Invocation {
  private final String appId;

  private final String microserviceName;

  public NonSwaggerInvocation(String appId, String microserviceName) {
    this.appId = appId;
    this.microserviceName = microserviceName;
  }

  @Override
  public String getSchemaId() {
    return "third-schema";
  }

  @Override
  public String getOperationName() {
    return "third-operation";
  }

  @Override
  public String getInvocationQualifiedName() {
    return microserviceName;
  }

  @Override
  public String getConfigTransportName() {
    return CoreConst.RESTFUL;
  }

  @Override
  public String getMicroserviceName() {
    return microserviceName;
  }

  @Override
  public String getAppId() {
    return appId;
  }
}
