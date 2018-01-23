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
package org.apache.servicecomb.demo.perf;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "impl")
@RequestMapping(path = "/v1")
public class Impl {
  private Intf intf;

  @Value(value = "${service_description.name}")
  public void setSelfMicroserviceName(String selfMicroserviceName) {
    // self: perf-1/perf-a
    // next: perf-2/perf-b
    char last = selfMicroserviceName.charAt(selfMicroserviceName.length() - 1);
    String nextMicroserviceName =
        selfMicroserviceName.substring(0, selfMicroserviceName.length() - 1) + (char) (last + 1);
    intf = Invoker.createProxy(nextMicroserviceName,
        "impl",
        Intf.class);
  }

  @GetMapping(path = "/syncQuery/{id}")
  public String syncQuery(@PathVariable(name = "id") String id,
      @RequestParam(name = "step") int step, @RequestParam(name = "all") int all,
      @RequestParam(name = "fromDB") boolean fromDB) {
    if (step == all) {
      if (fromDB) {
        return RedisClientUtils.syncQuery(id);
      }

      return buildFromMemoryResponse(id);
    }

    return intf.syncQuery(id, step + 1, all, fromDB);
  }

  public String buildFromMemoryResponse(String id) {
    return PerfConfiguration.buildResponse("memory", id);
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
      future.complete(buildFromMemoryResponse(id));
      return future;
    }

    return intf.asyncQuery(id, step + 1, all, fromDB);
  }
}
