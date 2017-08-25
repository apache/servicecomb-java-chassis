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

package io.servicecomb.core.executor;

import java.util.concurrent.Executor;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.core.definition.OperationMeta;
import io.servicecomb.foundation.common.utils.BeanUtils;

public final class ExecutorManager {
  private ExecutorManager() {
  }

  // 只会在初始化时执行，一点点重复的查找，没必要做缓存
  public static Executor findExecutor(OperationMeta operationMeta) {
    Executor executor = findByKey("cse.executors.Provider." + operationMeta.getSchemaQualifiedName());
    if (executor != null) {
      return executor;
    }

    // 尝试schema级别
    executor = findByKey("cse.executors.Provider." + operationMeta.getSchemaMeta().getName());
    if (executor != null) {
      return executor;
    }

    executor = findByKey("cse.executors.default");
    if (executor != null) {
      return executor;
    }

    return BeanUtils.getBean("cse.executor.default");
  }

  protected static Executor findByKey(String beanIdKey) {
    String beanId = DynamicPropertyFactory.getInstance().getStringProperty(beanIdKey, null).get();
    if (beanId != null) {
      return BeanUtils.getBean(beanId);
    }

    return null;
  }
}
