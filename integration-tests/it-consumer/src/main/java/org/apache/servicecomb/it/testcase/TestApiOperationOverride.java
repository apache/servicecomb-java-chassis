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

import org.apache.servicecomb.it.Consumers;
import org.junit.Assert;
import org.junit.Test;

import io.swagger.annotations.ApiOperation;

public class TestApiOperationOverride {
  interface OptionalIntf {

    @ApiOperation(value = "", nickname = "sayHi")
    String sayHello();

    @ApiOperation(value = "", nickname = "sayHello")
    String sayHello(String name);

    @ApiOperation(value = "", nickname = "sayHello")
    CompletableFuture<String> sayHelloAsync(String name);

    @ApiOperation(value = "", nickname = "sayHi")
    CompletableFuture<String> sayHelloAsync();
  }

  private static Consumers<OptionalIntf> consumersPojo = new Consumers<>("apiOpertionPojoSchemaTest",
      OptionalIntf.class);

  private static Consumers<OptionalIntf> consumersJaxrs = new Consumers<>("apiOperationJaxrsSchemaTest",
      OptionalIntf.class);

  private static Consumers<OptionalIntf> consumersSpringmvc = new Consumers<>("apiOperationSpringmvcSchemaTest",
      OptionalIntf.class);

  @Test
  public void consumersPojo_A_intf() {
    Assert.assertEquals("ApiOpertionPojoSchema#sayHello", consumersPojo.getIntf().sayHello());
  }

  @Test
  public void consumersPojo_B_intf() {
    Assert.assertEquals("value", consumersPojo.getIntf().sayHello("value"));
  }

  @Test
  public void consumersPojo_C_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("value", consumersPojo.getIntf().sayHelloAsync("value").get());
  }

  @Test
  public void consumersPojo_D_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("ApiOpertionPojoSchema#sayHello", consumersPojo.getIntf().sayHelloAsync().get());
  }

  @Test
  public void consumersJaxrs_A_intf() {
    Assert.assertEquals("ApiOperationJaxrsSchema#sayHello", consumersJaxrs.getIntf().sayHello());
  }

  @Test
  public void consumersJaxrs_B_intf() {
    Assert.assertEquals("value", consumersJaxrs.getIntf().sayHello("value"));
  }

  @Test
  public void consumersJaxrs_C_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("value", consumersJaxrs.getIntf().sayHelloAsync("value").get());
  }

  @Test
  public void consumersJaxrs_D_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("ApiOperationJaxrsSchema#sayHello", consumersJaxrs.getIntf().sayHelloAsync().get());
  }

  @Test
  public void consumersSpringmvc_A_intf() {
    Assert.assertEquals("ApiOperationSpringmvcSchema#sayHello", consumersSpringmvc.getIntf().sayHello());
  }

  @Test
  public void consumersSpringmvc_B_intf() {
    Assert.assertEquals("value", consumersSpringmvc.getIntf().sayHello("value"));
  }

  @Test
  public void consumersSpringmvc_C_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("value", consumersSpringmvc.getIntf().sayHelloAsync("value").get());
  }

  @Test
  public void consumersSpringmvc_D_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("ApiOperationSpringmvcSchema#sayHello", consumersSpringmvc.getIntf().sayHelloAsync().get());
  }
}
