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

package io.servicecomb.samples.springmvc.provider;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.netty.util.concurrent.CompleteFuture;
import io.servicecomb.provider.rest.common.RestSchema;
import io.servicecomb.samples.common.schema.Hello;
import io.servicecomb.samples.common.schema.HelloAsync;
import io.servicecomb.samples.common.schema.models.Person;

@RestSchema(schemaId = "springmvcHelloAsync")
@RequestMapping(path = "/springmvchelloasync", produces = MediaType.APPLICATION_JSON)
public class SpringmvcHelloAsyncImpl implements HelloAsync {

  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(8);


  @Override
  @RequestMapping(path = "/sayhi", method = RequestMethod.POST)
  public CompletableFuture<Person> sayHi(@RequestParam(name = "name") String name) {
    Person person = new Person();
    CompletableFuture<Person> result = new CompletableFuture<Person>();
    person.setName("Hello" + name);
    executorService.schedule(() -> result.complete(person), 2000, TimeUnit.MILLISECONDS);
    return result;
  }

  @Override
  @RequestMapping(path = "/sayhello", method = RequestMethod.POST)
  public CompletableFuture<String> sayHello(@RequestBody Person person) {
    CompletableFuture<String> result = new CompletableFuture<String>();
    executorService.schedule(() -> result.complete("Hello person " + person.getName()), 2000, TimeUnit.MILLISECONDS);
    return result;
  }
}
