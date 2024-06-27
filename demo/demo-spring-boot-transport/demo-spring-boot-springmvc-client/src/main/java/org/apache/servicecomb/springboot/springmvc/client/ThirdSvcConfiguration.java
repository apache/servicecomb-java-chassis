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

import org.apache.servicecomb.provider.pojo.Invoker;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Configuration
public class ThirdSvcConfiguration {
  @RequestMapping(path = "/")
  public interface ReactiveStreamClient {
    class Model {
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
    Publisher<String> sseString();

    @GetMapping("/sseModel")
    Publisher<Model> sseModel();
  }

  @Bean("reactiveStreamProvider")
  public ReactiveStreamClient reactiveStreamProvider() {
    return Invoker.createProxy("springmvc", "ReactiveStreamController", ReactiveStreamClient.class);
  }
}
