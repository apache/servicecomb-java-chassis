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
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.HystrixObservableCommand;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import rx.Observable;

/**
 * 接管调用链的处理流程，处理完成后，将结果交给调用链。额外还提供了请求缓存的功能和容错/降级处理能力。
 *
 */
public abstract class BizkeeperCommand extends HystrixObservableCommand<Response> {
  private static final Logger LOG = LoggerFactory.getLogger(BizkeeperCommand.class);

  private final Invocation invocation;

  private final String type;

  protected BizkeeperCommand(String type, Invocation invocation, HystrixObservableCommand.Setter setter) {
    super(setter);
    this.type = type;
    this.invocation = invocation;
  }

  @Override
  protected String getCacheKey() {
    if (HystrixRequestContext.isCurrentThreadInitialized()) {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getCommandGroup().name());
      sb.append("-");
      sb.append(this.getCommandKey().name());
      return sb.toString();
    } else {
      return super.getCacheKey();
    }
  }

  @Override
  protected Observable<Response> resumeWithFallback() {
    Throwable cause = getFailedExecutionException();
    Observable<Response> observable = Observable.create(f -> {
      try {
        f.onNext(FallbackPolicyManager.getFallbackResponse(type, cause, invocation));
        f.onCompleted();
      } catch (Exception e) {
        LOG.warn("fallback failed due to:" + e.getMessage());
        throw e;
      }
    });
    return observable;
  }

  @Override
  protected Observable<Response> construct() {
    Observable<Response> observable = Observable.create(f -> {
      try {
        invocation.next(resp -> {
          if (isFailedResponse(resp)) {
            // e should implements toString
            LOG.warn("bizkeeper command {} failed due to {}", invocation.getInvocationQualifiedName(),
                resp.getResult());
            f.onError(resp.getResult());
            FallbackPolicyManager.record(type, invocation, resp, false);
          } else {
            f.onNext(resp);
            f.onCompleted();
            FallbackPolicyManager.record(type, invocation, resp, true);
          }
        });
      } catch (Exception e) {
        LOG.warn("bizkeeper command {} execute failed due to {}", invocation.getInvocationQualifiedName(),
            e.getClass().getName());
        f.onError(e);
      }
    });
    return observable;
  }

  protected abstract boolean isFailedResponse(Response resp);
}
