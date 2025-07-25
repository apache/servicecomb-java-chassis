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
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.sse.SseEventResponseEntity;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestReactiveStream implements CategorizedTestCase {
  RestTemplate restTemplate = RestTemplateBuilder.create();

  private static final String SERVER = "servicecomb://springmvc";

  @Override
  public void testRestTransport() throws Exception {
    testSseString();
    testSseStringWithParam();
    testSseModel();
    testSseResponseEntity();
  }

  private void testSseStringWithParam() throws Exception {
    System.out.println("=============start testSseStringWithParam==================");
    Publisher<SseEventResponseEntity<?>> result
        = restTemplate.getForObject(SERVER + "/sseStringWithParam?name=d", Publisher.class);
    TestMgr.check("abcd", buildStringBuffer(result));
  }

  private void testSseString() throws Exception {
    System.out.println("=============start testSseString==================");
    Publisher<SseEventResponseEntity<?>> result
        = restTemplate.getForObject(SERVER + "/sseString", Publisher.class);
    TestMgr.check("abc", buildStringBuffer(result));
  }

  private void testSseResponseEntity() throws Exception {
    System.out.println("=============start testSseResponseEntity==================");
    Publisher<SseEventResponseEntity<?>> result
        = restTemplate.getForObject(SERVER + "/sseResponseEntity", Publisher.class);
    TestMgr.check("test0jack0test1jack1test2jack2", buildStringBuffer(result));
  }

  private void testSseModel() throws Exception {
    System.out.println("=============start testSseModel==================");
    Publisher<SseEventResponseEntity<?>> result
        = restTemplate.getForObject(SERVER + "/sseModel", Publisher.class);
    TestMgr.check("jack0jack1jack2jack3jack4", buildStringBuffer(result));
  }

  private String buildStringBuffer(Publisher<SseEventResponseEntity<?>> result) throws Exception {
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
      public void onNext(SseEventResponseEntity<?> responseEntity) {
        if (responseEntity.getData() instanceof String) {
          System.out.println("=========onNext=============>" + responseEntity.getData());
          buffer.append(responseEntity.getData());
        }
        if (responseEntity.getData() instanceof Model model) {
          if (!StringUtils.isEmpty(responseEntity.getEvent())) {
            buffer.append(responseEntity.getEvent());
          }
          buffer.append(model.getName()).append(model.getAge());
        }
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
    countDownLatch.await(20, TimeUnit.SECONDS);
    System.out.println("=========result=============>" + buffer.toString());
    return buffer.toString();
  }
}
