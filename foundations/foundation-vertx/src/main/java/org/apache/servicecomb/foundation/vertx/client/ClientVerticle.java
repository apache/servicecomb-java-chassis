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

package org.apache.servicecomb.foundation.vertx.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;

public class ClientVerticle<CLIENT_POOL> extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientVerticle.class);

  public static final String CLIENT_MGR = "clientMgr";

  @SuppressWarnings("unchecked")
  @Override
  public void start() throws Exception {
    try {
      ClientPoolManager<CLIENT_POOL> clientMgr = (ClientPoolManager<CLIENT_POOL>) config().getValue(CLIENT_MGR);
      clientMgr.createClientPool(context);
    } catch (Throwable e) {
      // vert.x got some states that not print error and execute call back in VertexUtils.blockDeploy, we add a log our self.
      LOGGER.error("", e);
      throw e;
    }
  }
}
