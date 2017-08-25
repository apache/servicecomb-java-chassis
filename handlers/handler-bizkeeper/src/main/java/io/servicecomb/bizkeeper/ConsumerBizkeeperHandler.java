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

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;

import io.servicecomb.core.Invocation;

/**
 * 客户端调用链处理流程
 *
 */
public class ConsumerBizkeeperHandler extends BizkeeperHandler {
  private static final String COMMAND_GROUP = "Consumer";

  public ConsumerBizkeeperHandler() {
    super(COMMAND_GROUP);
  }

  @Override
  protected BizkeeperCommand createBizkeeperCommand(Invocation invocation) {
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(true)
        .withRequestLogEnabled(false);
    setCommonProperties(invocation, setter);

    BizkeeperCommand command = new ConsumerBizkeeperCommand(groupname, invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey(groupname, invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey(groupname, invocation))
            .andCommandPropertiesDefaults(setter));
    return command;
  }
}
