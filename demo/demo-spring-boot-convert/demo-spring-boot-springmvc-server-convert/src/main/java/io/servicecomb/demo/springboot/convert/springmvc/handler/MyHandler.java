/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.demo.springboot.convert.springmvc.handler;

import io.servicecomb.core.Invocation;
import io.servicecomb.core.handler.impl.AbstractHandler;
import io.servicecomb.swagger.invocation.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyHandler extends AbstractHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyHandler.class);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResponse) throws Exception {
    LOGGER.info("If you see this log, that means this demo project has been converted to ServiceComb framework.");

    invocation.next(response -> {
      asyncResponse.handle(response);
    });
  }
}
