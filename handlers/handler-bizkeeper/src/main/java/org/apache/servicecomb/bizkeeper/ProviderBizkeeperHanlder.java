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

package org.apache.servicecomb.bizkeeper;

import org.apache.servicecomb.core.Invocation;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;

/**
 * 服务端调用链处理流程
 *
 */
public class ProviderBizkeeperHanlder extends BizkeeperHandler {
  private static final String COMMAND_GROUP = "Provider";

  public ProviderBizkeeperHanlder() {
    super(COMMAND_GROUP);
  }

  @Override
  protected BizkeeperCommand createBizkeeperCommand(Invocation invocation) {
    HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter()
        .withRequestCacheEnabled(false)
        .withRequestLogEnabled(false);
    setCommonProperties(invocation, setter);

    BizkeeperCommand command = new ProviderBizkeeperCommand(groupname, invocation,
        HystrixObservableCommand.Setter
            .withGroupKey(CommandKey.toHystrixCommandGroupKey(groupname, invocation))
            .andCommandKey(CommandKey.toHystrixCommandKey(groupname, invocation))
            .andCommandPropertiesDefaults(setter));
    return command;
  }
}
