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

package io.servicecomb.core.handler.impl;

import io.servicecomb.core.Const;
import io.servicecomb.core.Invocation;
import io.servicecomb.core.exception.ExceptionUtils;
import io.servicecomb.swagger.engine.SwaggerProducerOperation;
import io.servicecomb.swagger.invocation.AsyncResponse;

public class ProducerOperationHandler extends AbstractHandler {
  public static final ProducerOperationHandler INSTANCE = new ProducerOperationHandler();

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    SwaggerProducerOperation producerOperation =
        invocation.getOperationMeta().getExtData(Const.PRODUCER_OPERATION);
    if (producerOperation == null) {
      asyncResp.producerFail(
          ExceptionUtils.producerOperationNotExist(invocation.getSchemaId(),
              invocation.getOperationName()));
      return;
    }
    producerOperation.invoke(invocation, asyncResp);
  }
}
