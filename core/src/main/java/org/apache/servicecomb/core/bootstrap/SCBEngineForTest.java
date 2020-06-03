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

package org.apache.servicecomb.core.bootstrap;

import static org.apache.servicecomb.core.executor.ExecutorManager.EXECUTOR_GROUP_THREADPOOL;

import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.executor.GroupExecutor;
import org.apache.servicecomb.core.filter.FilterChainsManager;
import org.apache.servicecomb.core.filter.FilterManager;
import org.apache.servicecomb.core.filter.config.TransportFiltersConfig;
import org.apache.servicecomb.foundation.common.event.EventManager;
import org.apache.servicecomb.foundation.common.event.SimpleEventBus;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;

/**
 * not depend on remote service registry and spring context
 */
public class SCBEngineForTest extends SCBEngine {
  public SCBEngineForTest() {
    getExecutorManager().registerExecutor(EXECUTOR_GROUP_THREADPOOL, new GroupExecutor().init());
    setFilterChainsManager(new FilterChainsManager()
        .setTransportFiltersConfig(new TransportFiltersConfig())
        .setFilterManager(new FilterManager()));
  }

  @Override
  public synchronized void destroy() {
    super.destroy();

    ReflectUtils.setField(SCBEngine.class, null, "INSTANCE", null);

    EventManager.eventBus = new SimpleEventBus();
  }
}
