/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.servlet;

import io.servicecomb.common.rest.RestProducerInvocation;
import io.servicecomb.common.rest.definition.RestOperationMeta;
import io.servicecomb.common.rest.filter.HttpServerFilter;
import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.vertx.http.StandardHttpServletRequestEx;

public class RestServletProducerInvocation extends RestProducerInvocation {
  @Override
  protected RestOperationMeta findRestOperation() {
    RestOperationMeta restOperationMeta = super.findRestOperation();

    boolean cacheRequest = collectCacheRequest(restOperationMeta.getOperationMeta());
    ((StandardHttpServletRequestEx) requestEx).setCacheRequest(cacheRequest);

    return restOperationMeta;
  }

  protected boolean collectCacheRequest(OperationMeta operationMeta) {
    for (HttpServerFilter filter : httpServerFilters) {
      if (filter.needCacheRequest(operationMeta)) {
        return true;
      }
    }
    return false;
  }
}
