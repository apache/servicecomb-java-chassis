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

package io.servicecomb.foundation.vertx.client;

import io.vertx.core.AbstractVerticle;

public abstract class AbstractClientVerticle<CLIENT_POOL> extends AbstractVerticle
    implements ClientPoolFactory<CLIENT_POOL> {
  public static final String CLIENT_MGR = "clientMgr";

  public static final String POOL_COUNT = "poolCount";

  public static final String CLIENT_OPTIONS = "clientOptions";

  @SuppressWarnings("unchecked")
  @Override
  public void start() throws Exception {
    ClientPoolManager<CLIENT_POOL> clientMgr = (ClientPoolManager<CLIENT_POOL>) config().getValue(CLIENT_MGR);
    Integer poolCount = config().getInteger(POOL_COUNT);

    NetThreadData<CLIENT_POOL> netThreadData = new NetThreadData<>(this, poolCount);
    clientMgr.addNetThread(netThreadData);
  }
}
