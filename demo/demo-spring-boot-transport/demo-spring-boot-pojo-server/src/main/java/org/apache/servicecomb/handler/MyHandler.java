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

package org.apache.servicecomb.handler;

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.demo.server.User;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyHandler implements Handler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyHandler.class);

  public static final String SPLITPARAM_RESPONSE_USER_SUFFIX = "(modified by MyHandler)";

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    LOGGER.info("If you see this log, that means this demo project has been converted to ServiceComb framework.");

    invocation.next(response -> {
      if (invocation.getOperationMeta().getSchemaQualifiedName().equals("server.splitParam")) {
        User user = response.getResult();
        user.setName(user.getName() + SPLITPARAM_RESPONSE_USER_SUFFIX);
        asyncResp.handle(response);
      } else {
        asyncResp.handle(response);
      }
    });
  }
}
