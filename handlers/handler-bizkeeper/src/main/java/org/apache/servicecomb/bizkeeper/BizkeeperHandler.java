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

import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixInvokable;
import com.netflix.hystrix.HystrixObservable;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;

import rx.Observable;

/**
 * 提供createBizkeeperCommand抽象接口来创建不同的处理链实例。
 *
 */
public abstract class BizkeeperHandler implements Handler {
  private static final Logger LOG = LoggerFactory.getLogger(BizkeeperHandler.class);

  protected final String groupname;

  static {
    try {
      HystrixPlugins.getInstance().registerPropertiesStrategy(HystrixPropertiesStrategyExt.getInstance());
    } catch (IllegalStateException e) {
      LOG.warn("Hystrix properties already registered. Dynamic configuration may not work.", e);
    }
    try {
      HystrixPlugins.getInstance().registerCommandExecutionHook(new HystrixCommandExecutionHook() {
        @Override
        public <T> Exception onExecutionError(HystrixInvokable<T> commandInstance, Exception e) {
          LOG.warn("bizkeeper execution error", e);
          return e; //by default, just pass through
        }
      });
    } catch (IllegalStateException e) {
      LOG.warn("HystrixCommandExecutionHook already registered.", e);
    }
  }

  private BizkeeperHandlerDelegate delegate;

  public BizkeeperHandler(String groupname) {
    this.groupname = groupname;
    delegate = new BizkeeperHandlerDelegate(this);
  }

  protected abstract BizkeeperCommand createBizkeeperCommand(Invocation invocation);

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResp) {
    HystrixObservable<Response> command = delegate.createBizkeeperCommand(invocation);

    Observable<Response> observable = command.toObservable();
    observable.subscribe(asyncResp::complete, error -> {
      LOG.warn("catch error in bizkeeper:" + error.getMessage());
      asyncResp.fail(invocation.getInvocationType(), error);
    }, () -> {

    });
  }

  protected void setCommonProperties(Invocation invocation, HystrixCommandProperties.Setter setter) {
  }
}
