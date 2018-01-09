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
package io.servicecomb.demo.perf;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "impl")
@RequestMapping(path = "/v1")
public class Impl {
  private Intf intf;

  public Impl() {
    //    intf = Invoker.createProxy(perfConfiguration.getNextMicroserviceName(),
    //        "impl",
    //        Intf.class);
  }

  @GetMapping(path = "/syncQuery/{id}")
  public String syncQuery(@PathVariable(name = "id") String id,
      @RequestParam(name = "step") int step, @RequestParam(name = "all") int all,
      @RequestParam(name = "fromDB") boolean fromDB) {
    if (step == all) {
      if (fromDB) {
        return RedisClientUtils.syncQuery(id);
      }

      return new StringBuilder(64 + PerfConfiguration.responseData.length())
          .append(id)
          .append(" from memory: ")
          .append(PerfConfiguration.responseData)
          .toString();
    }

    return intf.syncQuery(id, step, all, fromDB);
  }

  @GetMapping(path = "/asyncQuery/{id}")
  public CompletableFuture<String> asyncQuery(@PathVariable(name = "id") String id,
      @RequestParam(name = "step") int step, @RequestParam(name = "all") int all,
      @RequestParam(name = "fromDB") boolean fromDB) {
    if (step == all) {
      if (fromDB) {
        return RedisClientUtils.asyncQuery(id);
      }

      CompletableFuture<String> future = new CompletableFuture<>();
      future.complete("value of " + id + " from memory.");
      return future;
    }

    return intf.asyncQuery(id, step, all, fromDB);
  }
}
