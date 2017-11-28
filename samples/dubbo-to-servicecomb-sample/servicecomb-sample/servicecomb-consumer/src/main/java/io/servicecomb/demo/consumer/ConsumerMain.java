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

package io.servicecomb.demo.consumer;

import java.util.concurrent.CompletableFuture;

import org.springframework.context.ApplicationContext;

import io.servicecomb.demo.api.AnotherService;
import io.servicecomb.demo.api.SomeService;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;

public class ConsumerMain {
  public static void main(String[] args) throws Exception {
    Log4jUtils.init();
    BeanUtils.init();

    System.out.println("ServiceComb Consumer started successfully...");

    ApplicationContext context = BeanUtils.getContext();
    SomeService someService = (SomeService) context.getBean("someServiceRef");

    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> someService.sayHello("world"));
    System.err.println(future.get());

    AnotherService anotherService = (AnotherService) context.getBean("anotherServiceRef");

    future = CompletableFuture.supplyAsync(() -> anotherService.sayHi("world"));
    System.err.println(future.get());
  }
}
