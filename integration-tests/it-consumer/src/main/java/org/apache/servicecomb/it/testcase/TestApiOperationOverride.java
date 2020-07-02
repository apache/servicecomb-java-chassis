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
package org.apache.servicecomb.it.testcase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.it.Consumers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import io.swagger.annotations.ApiOperation;

public class TestApiOperationOverride {
  interface OptionalIntf {
    @ApiOperation(value = "", nickname = "sayHi")
    String sayHello(Long index);

    @ApiOperation(value = "", nickname = "sayHello")
    String sayHello(String name);

    @ApiOperation(value = "", nickname = "sayHello")
    CompletableFuture<String> sayHelloAsync(String name);

    @ApiOperation(value = "", nickname = "sayHi")
    CompletableFuture<String> sayHelloAsync(Long index);

    @ApiOperation(value = "", nickname = "sayHi")
    ResponseEntity<String> sayHelloEntity(Long index);

    @ApiOperation(value = "", nickname = "sayHello")
    ResponseEntity<String> sayHelloEntity(String name);

    @ApiOperation(value = "", nickname = "sayHi")
    CompletableFuture<ResponseEntity<String>> sayHelloEntityAsync(Long index);

    @ApiOperation(value = "", nickname = "sayHello")
    CompletableFuture<ResponseEntity<String>> sayHelloEntityAsync(String name);
  }

  private static AtomicLong indexGenerator = new AtomicLong();

  private static Consumers<OptionalIntf> consumersPojo = new Consumers<>("apiOpertionPojoSchemaTest",
      OptionalIntf.class);

  private static Consumers<OptionalIntf> consumersJaxrs = new Consumers<>("apiOperationJaxrsSchemaTest",
      OptionalIntf.class);

  private static Consumers<OptionalIntf> consumersSpringmvc = new Consumers<>("apiOperationSpringmvcSchemaTest",
      OptionalIntf.class);

  @Test
  public void consumersPojo_sayHi_intf() {
    long index = generateIndex();
    Assert.assertEquals("ApiOpertionPojoSchema#sayHello" + index, consumersPojo.getIntf().sayHello(index));
  }

  @Test
  public void consumersPojo_sayHello_intf() {
    long index = generateIndex();
    Assert.assertEquals("value" + index, consumersPojo.getIntf().sayHello("value" + index));
  }

  @Test
  public void consumersPojo_sayHelloAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("value" + index, consumersPojo.getIntf().sayHelloAsync("value" + index).get());
  }

  @Test
  public void consumersPojo_sayHiAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("ApiOpertionPojoSchema#sayHello" + index, consumersPojo.getIntf().sayHelloAsync(index).get());
  }

  @Test
  public void consumersPojo_sayHiEntity_intf() {
    long index = generateIndex();
    Assert.assertEquals("ApiOpertionPojoSchema#sayHello" + index,
        consumersPojo.getIntf().sayHelloEntity(index).getBody());
  }

  @Test
  public void consumersPojo_sayHelloEntity_intf() {
    long index = generateIndex();
    Assert.assertEquals("value" + index,
        consumersPojo.getIntf().sayHelloEntity("value" + index).getBody());
  }

  @Test
  public void consumersPojo_sayHiEntityAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("ApiOpertionPojoSchema#sayHello" + index,
        consumersPojo.getIntf().sayHelloEntityAsync(index).get().getBody());
  }

  @Test
  public void consumersPojo_sayHelloEntityAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("value" + index,
        consumersPojo.getIntf().sayHelloEntityAsync("value" + index).get().getBody());
  }

  @Test
  public void consumersJaxrs_sayHi_intf() {
    long index = generateIndex();
    Assert.assertEquals("ApiOperationJaxrsSchema#sayHello" + index, consumersJaxrs.getIntf().sayHello(index));
  }

  @Test
  public void consumersJaxrs_sayHello_intf() {
    long index = generateIndex();
    Assert.assertEquals("value" + index, consumersJaxrs.getIntf().sayHello("value" + index));
  }

  @Test
  public void consumersJaxrs_sayHelloAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("value" + index, consumersJaxrs.getIntf().sayHelloAsync("value" + index).get());
  }

  @Test
  public void consumersJaxrs_sayHiAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert
        .assertEquals("ApiOperationJaxrsSchema#sayHello" + index, consumersJaxrs.getIntf().sayHelloAsync(index).get());
  }

  @Test
  public void consumersJaxrs_sayHiEntity_intf() {
    long index = generateIndex();
    Assert.assertEquals("ApiOperationJaxrsSchema#sayHello" + index,
        consumersJaxrs.getIntf().sayHelloEntity(index).getBody());
  }

  @Test
  public void consumersJaxrs_sayHelloEntity_intf() {
    long index = generateIndex();
    Assert.assertEquals("value" + index,
        consumersJaxrs.getIntf().sayHelloEntity("value" + index).getBody());
  }

  @Test
  public void consumersJaxrs_sayHiEntityAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("ApiOperationJaxrsSchema#sayHello" + index,
        consumersJaxrs.getIntf().sayHelloEntityAsync(index).get().getBody());
  }

  @Test
  public void consumersJaxrs_sayHelloEntityAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("value" + index,
        consumersJaxrs.getIntf().sayHelloEntityAsync("value" + index).get().getBody());
  }

  @Test
  public void consumersSpringmvc_sayHi_intf() {
    long index = generateIndex();
    Assert.assertEquals("ApiOperationSpringmvcSchema#sayHello" + index, consumersSpringmvc.getIntf().sayHello(index));
  }

  @Test
  public void consumersSpringmvc_sayHello_intf() {
    long index = generateIndex();
    Assert.assertEquals("value" + index, consumersSpringmvc.getIntf().sayHello("value" + index));
  }

  @Test
  public void consumersSpringmvc_sayHelloAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("value" + index, consumersSpringmvc.getIntf().sayHelloAsync("value" + index).get());
  }

  @Test
  public void consumersSpringmvc_sayHiAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("ApiOperationSpringmvcSchema#sayHello" + index,
        consumersSpringmvc.getIntf().sayHelloAsync(index).get());
  }

  @Test
  public void consumersSpringmvc_sayHiEntity_intf() {
    long index = generateIndex();
    Assert.assertEquals("ApiOperationSpringmvcSchema#sayHello" + index,
        consumersSpringmvc.getIntf().sayHelloEntity(index).getBody());
  }

  @Test
  public void consumersSpringmvc_sayHelloEntity_intf() {
    long index = generateIndex();
    Assert.assertEquals("value" + index,
        consumersSpringmvc.getIntf().sayHelloEntity("value" + index).getBody());
  }

  @Test
  public void consumersSpringmvc_sayHiEntityAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("ApiOperationSpringmvcSchema#sayHello" + index,
        consumersSpringmvc.getIntf().sayHelloEntityAsync(index).get().getBody());
  }

  @Test
  public void consumersSpringmvc_sayHelloEntityAsync_intf() throws ExecutionException, InterruptedException {
    long index = generateIndex();
    Assert.assertEquals("value" + index,
        consumersSpringmvc.getIntf().sayHelloEntityAsync("value" + index).get().getBody());
  }

  private long generateIndex() {
    return indexGenerator.getAndIncrement();
  }
}
