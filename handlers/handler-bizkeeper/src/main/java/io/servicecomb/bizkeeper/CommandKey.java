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

package io.servicecomb.bizkeeper;

import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

import io.servicecomb.core.Invocation;

/**
 * 创建对应的Key值
 *
 */
public final class CommandKey {
  private CommandKey() {
  }

  public static HystrixCommandGroupKey toHystrixCommandGroupKey(String type, Invocation invocation) {
    return HystrixCommandGroupKey.Factory
        .asKey(type + "." + invocation.getOperationMeta().getMicroserviceQualifiedName());
  }

  public static HystrixCommandKey toHystrixCommandKey(String type, Invocation invocation) {
    return HystrixCommandKey.Factory
        .asKey(type + "." + invocation.getOperationMeta().getMicroserviceQualifiedName());
  }
}
