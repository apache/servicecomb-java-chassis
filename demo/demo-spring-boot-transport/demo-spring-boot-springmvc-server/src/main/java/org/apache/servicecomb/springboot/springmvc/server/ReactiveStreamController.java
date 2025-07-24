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
package org.apache.servicecomb.springboot.springmvc.server;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.sse.SseEventResponseEntity;
import org.reactivestreams.Publisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.reactivex.rxjava3.core.Flowable;

@RestSchema(schemaId = "ReactiveStreamController")
@RequestMapping(path = "/")
public class ReactiveStreamController {
  public static class Model {
    private String name;

    private int age;

    public Model() {

    }

    public Model(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public int getAge() {
      return age;
    }

    public Model setAge(int age) {
      this.age = age;
      return this;
    }

    public String getName() {
      return name;
    }

    public Model setName(String name) {
      this.name = name;
      return this;
    }
  }

  @GetMapping("/sseString")
  public Publisher<String> sseString() {
    return Flowable.fromArray("a", "b", "c");
  }

  @GetMapping("/sseStringWithParam")
  public Publisher<String> sseStringWithParam(@RequestParam(name = "name") String name) {
    return Flowable.fromArray("a", "b", "c", name);
  };

  @GetMapping("/sseModel")
  public Publisher<Model> sseModel() {
    return Flowable.intervalRange(0, 5, 0, 1, TimeUnit.SECONDS)
        .map(item -> new Model("jack", item.intValue()));
  }

  @GetMapping("/sseResponseEntity")
  public Publisher<SseEventResponseEntity<Model>> sseResponseEntity() {
    AtomicInteger index = new AtomicInteger(0);
    return Flowable.intervalRange(0, 3, 0, 1, TimeUnit.SECONDS)
        .map(item -> new SseEventResponseEntity<Model>()
            .event("test" + index)
            .eventId(index.getAndIncrement())
            .retry(System.currentTimeMillis())
            .data(new Model("jack", item.intValue())));
  };
}
