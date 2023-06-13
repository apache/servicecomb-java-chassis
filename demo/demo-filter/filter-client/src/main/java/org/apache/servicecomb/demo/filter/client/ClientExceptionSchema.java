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
package org.apache.servicecomb.demo.filter.client;

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@RestSchema(schemaId = "ClientExceptionSchema")
@RequestMapping(path = "/exception", produces = MediaType.APPLICATION_JSON_VALUE)
public class ClientExceptionSchema {
  interface IExceptionSchema {
    boolean blockingException();

    CompletableFuture<Boolean> reactiveException();
  }

  private IExceptionSchema exceptionSchema;

  private RestTemplate restTemplate = RestTemplateBuilder.create();

  @RpcReference(microserviceName = "filterServer", schemaId = "ExceptionSchema")
  public void setExceptionSchema(IExceptionSchema exceptionSchema) {
    this.exceptionSchema = exceptionSchema;
  }

  @GetMapping(path = "/blockingExceptionRestTemplate")
  public boolean blockingExceptionRestTemplate() {
    return restTemplate.getForObject(
        "servicecomb://filterServer/exception/blockingException", boolean.class);
  }

  @GetMapping(path = "/blockingExceptionReference")
  public boolean blockingExceptionReference() {
    return exceptionSchema.blockingException();
  }

  @GetMapping(path = "/blockingExceptionInvoker")
  public boolean blockingExceptionInvoker() {
    return InvokerUtils.syncInvoke("filterServer",
        "ExceptionSchema", "blockingException", null, boolean.class);
  }

  @GetMapping(path = "/reactiveExceptionReference")
  public CompletableFuture<Boolean> reactiveExceptionReference() {
    return exceptionSchema.reactiveException();
  }
}
