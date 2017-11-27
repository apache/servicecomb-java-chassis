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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.rpc.RpcContext;

import io.servicecomb.demo.api.AnotherService;
import io.servicecomb.demo.api.SomeService;

public class ConsumerMain {
  public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
    @SuppressWarnings(
        { "resource", "unused" })
    ApplicationContext context = new ClassPathXmlApplicationContext("conf/applicationContext.xml");
    System.out.println("Dubbo Consumer started successfully...");

    SomeService someService = (SomeService) context.getBean("someServiceRef");
    someService.sayHello("world");
    Future<String> future = RpcContext.getContext().getFuture();
    System.err.println(future.get());

    AnotherService anotherService = (AnotherService) context.getBean("anotherServiceRef");
    anotherService.sayHi("world");
    future = RpcContext.getContext().getFuture();
    System.err.println(future.get());

    System.in.read();
  }
}
