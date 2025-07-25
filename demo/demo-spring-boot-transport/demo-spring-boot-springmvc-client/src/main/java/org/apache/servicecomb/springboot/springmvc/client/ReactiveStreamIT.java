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

package org.apache.servicecomb.springboot.springmvc.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.model.Model;
import org.apache.servicecomb.springboot.springmvc.client.ThirdSvcConfiguration.ReactiveStreamClient;
import org.apache.servicecomb.swagger.sse.SseEventResponseEntity;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ReactiveStreamIT implements CategorizedTestCase {
  @Autowired
  @Qualifier("reactiveStreamProvider")
  ReactiveStreamClient reactiveStreamProvider;

  @Override
  public void testRestTransport() throws Exception {
    testSseString(reactiveStreamProvider);
    testSseStringWithParam(reactiveStreamProvider);
    testSseModel(reactiveStreamProvider);
    testSseResponseEntity(reactiveStreamProvider);
  }

  private void testSseString(ReactiveStreamClient client) throws Exception {
    Publisher<String> result = client.sseString();
    TestMgr.check("abc", buildBufferString(result));
  }

  private void testSseStringWithParam(ReactiveStreamClient client) throws Exception {
    Publisher<String> result = client.sseStringWithParam("d");
    TestMgr.check("abcd", buildBufferString(result));
  }

  private String buildBufferString(Publisher<String> result) throws Exception {
    StringBuilder buffer = new StringBuilder();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    result.subscribe(new Subscriber<>() {
      Subscription subscription;

      @Override
      public void onSubscribe(Subscription s) {
        subscription = s;
        subscription.request(1);
      }

      @Override
      public void onNext(String s) {
        buffer.append(s);
        subscription.request(1);
      }

      @Override
      public void onError(Throwable t) {
        subscription.cancel();
        countDownLatch.countDown();
      }

      @Override
      public void onComplete() {
        countDownLatch.countDown();
      }
    });
    countDownLatch.await(10, TimeUnit.SECONDS);
    return buffer.toString();
  }

  private void testSseModel(ReactiveStreamClient client) throws Exception {
    Publisher<Model> result = client.sseModel();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    StringBuilder buffer = new StringBuilder();
    result.subscribe(new Subscriber<>() {
      Subscription subscription;

      @Override
      public void onSubscribe(Subscription s) {
        subscription = s;
        subscription.request(1);
      }

      @Override
      public void onNext(Model s) {
        buffer.append(s.getName()).append(s.getAge());
        subscription.request(1);
      }

      @Override
      public void onError(Throwable t) {
        subscription.cancel();
        countDownLatch.countDown();
      }

      @Override
      public void onComplete() {
        countDownLatch.countDown();
      }
    });
    countDownLatch.await(10, TimeUnit.SECONDS);
    TestMgr.check("jack0jack1jack2jack3jack4", buffer.toString());
  }

  private void testSseResponseEntity(ReactiveStreamClient client) throws Exception {
    Publisher<SseEventResponseEntity<Model>> result = client.sseResponseEntity();
    CountDownLatch countDownLatch = new CountDownLatch(1);
    StringBuilder buffer = new StringBuilder();
    result.subscribe(new Subscriber<>() {
      Subscription subscription;

      @Override
      public void onSubscribe(Subscription s) {
        subscription = s;
        subscription.request(1);
      }

      @Override
      public void onNext(SseEventResponseEntity<Model> responseEntity) {
        if (!StringUtils.isEmpty(responseEntity.getEvent())) {
          buffer.append(responseEntity.getEvent());
        }
        buffer.append(((Model) responseEntity.getData()).getName())
            .append(((Model) responseEntity.getData()).getAge());
        subscription.request(1);
      }

      @Override
      public void onError(Throwable t) {
        subscription.cancel();
        countDownLatch.countDown();
      }

      @Override
      public void onComplete() {
        countDownLatch.countDown();
      }
    });
    countDownLatch.await(10, TimeUnit.SECONDS);
    TestMgr.check("test0jack0test1jack1test2jack2", buffer.toString());
  }
}
