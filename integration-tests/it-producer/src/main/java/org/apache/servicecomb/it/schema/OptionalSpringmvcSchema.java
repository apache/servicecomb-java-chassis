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
package org.apache.servicecomb.it.schema;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "optionalSpringmvc")
@RequestMapping(path = "/v1/optionalSpringmvc")
public class OptionalSpringmvcSchema {
  @GetMapping("optional")
  public Optional<String> optional(String result) {
    return Optional.ofNullable(result);
  }

  @GetMapping("completableFutureOptional")
  public CompletableFuture<Optional<String>> completableFutureOptional(String result) {
    return CompletableFuture.completedFuture(Optional.ofNullable(result));
  }

  @GetMapping("responseEntityOptional")
  public ResponseEntity<Optional<String>> responseEntityOptional(String result) {
    return ResponseEntity.ok(Optional.ofNullable(result));
  }

  @GetMapping("completableFutureResponseEntityOptional")
  public CompletableFuture<ResponseEntity<Optional<String>>> completableFutureResponseEntityOptional(
      String result) {
    return CompletableFuture.completedFuture(ResponseEntity.ok(Optional.ofNullable(result)));
  }
}
