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
package org.apache.servicecomb.transport.rest.client;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.common.net.URIEndpointObject;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;

class FakeRestTransport extends AbstractTransport {
  @Override
  public String getName() {
    return null;
  }

  @Override
  public boolean init() {
    return false;
  }

  @Override
  public void send(Invocation invocation, AsyncResponse asyncResp) {

  }

  @Override
  public Object parseAddress(String address) {
    return new URIEndpointObject(address);
  }
}
