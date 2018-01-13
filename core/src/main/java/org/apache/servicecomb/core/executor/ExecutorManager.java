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

package org.apache.servicecomb.core.executor;

import java.util.concurrent.Executor;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;

import com.netflix.config.DynamicPropertyFactory;

public final class ExecutorManager {
  public static final String KEY_EXECUTORS_PREFIX = "cse.executors.Provider.";

  public static final String KEY_EXECUTORS_DEFAULT = "cse.executors.default";

  public static final String EXECUTOR_GROUP_THREADPOOL = "cse.executor.groupThreadPool";

  public static final String EXECUTOR_REACTIVE = "cse.executor.reactive";

  public static final String EXECUTOR_DEFAULT = EXECUTOR_GROUP_THREADPOOL;

  private ExecutorManager() {
  }

  // 只会在初始化时执行，一点点重复的查找，没必要做缓存
  public static Executor findExecutor(OperationMeta operationMeta) {
    return findExecutor(operationMeta, null);
  }

  public static Executor findExecutor(OperationMeta operationMeta, Executor defaultOperationExecutor) {
    Executor executor = findByKey(KEY_EXECUTORS_PREFIX + operationMeta.getSchemaQualifiedName());
    if (executor != null) {
      return executor;
    }

    if (defaultOperationExecutor != null) {
      return defaultOperationExecutor;
    }

    // 尝试schema级别
    executor = findByKey(KEY_EXECUTORS_PREFIX + operationMeta.getSchemaMeta().getName());
    if (executor != null) {
      return executor;
    }

    executor = findByKey(KEY_EXECUTORS_DEFAULT);
    if (executor != null) {
      return executor;
    }

    return BeanUtils.getBean(EXECUTOR_DEFAULT);
  }

  protected static Executor findByKey(String beanIdKey) {
    String beanId = DynamicPropertyFactory.getInstance().getStringProperty(beanIdKey, null).get();
    if (beanId != null) {
      return BeanUtils.getBean(beanId);
    }

    return null;
  }
}
