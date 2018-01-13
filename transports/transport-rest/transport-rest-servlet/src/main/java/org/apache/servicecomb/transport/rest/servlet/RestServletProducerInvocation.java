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

package org.apache.servicecomb.transport.rest.servlet;

import org.apache.servicecomb.common.rest.RestProducerInvocation;
import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.vertx.http.StandardHttpServletRequestEx;

public class RestServletProducerInvocation extends RestProducerInvocation {
  @Override
  protected void findRestOperation() {
    super.findRestOperation();

    boolean cacheRequest = collectCacheRequest(restOperationMeta.getOperationMeta());
    ((StandardHttpServletRequestEx) requestEx).setCacheRequest(cacheRequest);
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
