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
package org.apache.servicecomb.core.event;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.Response;

public class InvocationFinishEvent {
  private long nanoCurrent;

  private Invocation invocation;

  private Response response;

  public InvocationFinishEvent(Invocation invocation, Response response) {
    this.nanoCurrent = System.nanoTime();
    this.invocation = invocation;
    this.response = response;
  }

  public long getNanoCurrent() {
    return nanoCurrent;
  }

  public Invocation getInvocation() {
    return invocation;
  }

  public Response getResponse() {
    return response;
  }
}
