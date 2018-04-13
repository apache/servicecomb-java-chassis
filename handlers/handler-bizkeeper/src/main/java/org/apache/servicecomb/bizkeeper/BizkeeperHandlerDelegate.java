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

import com.netflix.hystrix.HystrixObservable;

import rx.Observable;
import rx.Subscription;
import rx.subjects.ReplaySubject;

public class BizkeeperHandlerDelegate {

  private BizkeeperHandler handler;

  public BizkeeperHandlerDelegate(BizkeeperHandler handler) {
    this.handler = handler;
  }

  protected HystrixObservable<Response> createBizkeeperCommand(Invocation invocation) {
    if (Configuration.INSTANCE.isFallbackForce(handler.groupname,
        invocation.getMicroserviceName(),
        invocation.getOperationMeta().getMicroserviceQualifiedName())) {
      return forceFallbackCommand(invocation);
    }
    return handler.createBizkeeperCommand(invocation);
  }

  protected HystrixObservable<Response> forceFallbackCommand(Invocation invocation) {
    return new HystrixObservable<Response>() {
      @Override
      public Observable<Response> observe() {
        ReplaySubject<Response> subject = ReplaySubject.create();
        final Subscription sourceSubscription = toObservable().subscribe(subject);
        return subject.doOnUnsubscribe(sourceSubscription::unsubscribe);
      }

      @Override
      public Observable<Response> toObservable() {
        return Observable.create(f -> {
          try {
            f.onNext(FallbackPolicyManager.getFallbackResponse(handler.groupname, null, invocation));
          } catch (Exception e) {
            f.onError(e);
          }
        });
      }
    };
  }
}
