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

import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class ExecutorManager {
  public static final String KEY_EXECUTORS_PREFIX = "servicecomb.executors.Provider.";

  public static final String KEY_EXECUTORS_DEFAULT = "servicecomb.executors.default";

  public static final String EXECUTOR_GROUP_THREAD_POOL = "servicecomb.executor.groupThreadPool";

  public static final String EXECUTOR_REACTIVE = "servicecomb.executor.reactive";

  private final Map<String, Executor> executors = new ConcurrentHashMapEx<>();

  static String EXECUTOR_DEFAULT = EXECUTOR_GROUP_THREAD_POOL;

  public static void setExecutorDefault(String executorDefault) {
    EXECUTOR_DEFAULT = executorDefault;
  }

  private Environment environment;

  public ExecutorManager() {
    registerExecutor(EXECUTOR_REACTIVE, new ReactiveExecutor());
  }

  @Autowired
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public void registerExecutor(String id, Executor executor) {
    Executor existing = executors.putIfAbsent(id, executor);
    if (existing != null) {
      throw new IllegalStateException(String.format(
          "duplicated executor, id=%s, old executor=%s, new executor=%s",
          id, existing.getClass().getName(), executor.getClass().getName()));
    }
  }

  // 只会在初始化时执行，一点点重复的查找，没必要做缓存
  public Executor findExecutor(OperationMeta operationMeta) {
    return findExecutor(operationMeta, null);
  }

  public Executor findExecutor(OperationMeta operationMeta, Executor defaultOperationExecutor) {
    // operation级别
    Executor executor = findByKey(KEY_EXECUTORS_PREFIX + operationMeta.getMicroserviceQualifiedName());
    if (executor != null) {
      return executor;
    }

    executor = findByKey(KEY_EXECUTORS_PREFIX + operationMeta.getSchemaQualifiedName());
    if (executor != null) {
      return executor;
    }

    if (defaultOperationExecutor != null) {
      return defaultOperationExecutor;
    }

    // schema级别
    executor = findByKey(
        KEY_EXECUTORS_PREFIX + operationMeta.getMicroserviceName() + '.' + operationMeta.getSchemaId());
    if (executor != null) {
      return executor;
    }

    executor = findByKey(KEY_EXECUTORS_PREFIX + operationMeta.getSchemaId());
    if (executor != null) {
      return executor;
    }

    // microservice级别
    executor = findByKey(KEY_EXECUTORS_PREFIX + operationMeta.getMicroserviceName());
    if (executor != null) {
      return executor;
    }

    // 寻找config-key指定的default
    executor = findByKey(KEY_EXECUTORS_DEFAULT);
    if (executor != null) {
      return executor;
    }

    return findExecutorById(EXECUTOR_DEFAULT);
  }

  protected Executor findByKey(String configKey) {
    String id = environment.getProperty(configKey);
    if (id == null) {
      return null;
    }

    return findExecutorById(id);
  }

  public Executor findExecutorById(String id) {
    Executor executor = executors.get(id);
    if (executor != null) {
      return executor;
    }

    return BeanUtils.getBean(id);
  }
}
