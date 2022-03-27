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
package org.apache.servicecomb.demo.filter.retry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// test cases for retry
@RestSchema(schemaId = "RetrySchema")
@RequestMapping(path = "/retry", produces = MediaType.APPLICATION_JSON_VALUE)
public class RetrySchema {
  private AtomicLong counter = new AtomicLong(0);

  @GetMapping(path = "/governance/successWhenRetry")
  public boolean successWhenRetry() {
    if (counter.getAndIncrement() % 3 != 0) {
      throw new InvocationException(Status.INTERNAL_SERVER_ERROR, "try again later.");
    }
    return true;
  }

  @GetMapping(path = "/governance/successWhenRetryAsync")
  public CompletableFuture<Boolean> successWhenRetryAsync() {
    CompletableFuture<Boolean> result = new CompletableFuture<>();
    if (counter.getAndIncrement() % 2 == 0) {
      result.completeExceptionally(new InvocationException(Status.INTERNAL_SERVER_ERROR, "try again later."));
    } else {
      result.complete(true);
    }
    return result;
  }
}
