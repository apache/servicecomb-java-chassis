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
package org.apache.servicecomb.authentication.consumer;

import java.util.Optional;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;

/**
 *
 * add token to context
 * Provider will get token for authentication
 *
 */
public class ConsumerAuthHandler implements Handler {

  private RSAConsumerTokenManager athenticationTokenManager = new RSAConsumerTokenManager();

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {

    Optional<String> token = Optional.ofNullable(athenticationTokenManager.getToken());
    if (!token.isPresent()) {
      asyncResp.consumerFail(
          new IllegalStateException("rejected by consumer authentication handler"));
      return;
    }
    invocation.addContext(Const.AUTH_TOKEN, token.get());
    invocation.next(asyncResp);
  }

  public void setAuthenticationTokenManager(RSAConsumerTokenManager authenticationTokenManager) {
    this.athenticationTokenManager = authenticationTokenManager;
  }
}
