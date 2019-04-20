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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.it.Consumers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

public class TestOptional {
  interface OptionalIntf {
    Optional<String> optional(String result);

    CompletableFuture<Optional<String>> completableFutureOptional(String result);

    ResponseEntity<Optional<String>> responseEntityOptional(String result);

    CompletableFuture<ResponseEntity<Optional<String>>> completableFutureResponseEntityOptional(String result);
  }

  private static Consumers<OptionalIntf> consumersPojo = new Consumers<>("optionalPojo", OptionalIntf.class);

  private static Consumers<OptionalIntf> consumersJaxrs = new Consumers<>("optionalJaxrs", OptionalIntf.class);

  private static Consumers<OptionalIntf> consumersSpringmvc = new Consumers<>("optionalSpringmvc", OptionalIntf.class);

  @Test
  public void optional_pojo_intf() {
    Assert.assertEquals("value", consumersPojo.getIntf().optional("value").get());
    Assert.assertFalse(consumersPojo.getIntf().optional(null).isPresent());
  }

  @Test
  public void completableFutureOptional_pojo_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("value", consumersPojo.getIntf().completableFutureOptional("value").get().get());
    Assert.assertFalse(consumersPojo.getIntf().completableFutureOptional(null).get().isPresent());
  }

  @Test
  public void optional_jaxrs_intf() {
    Assert.assertEquals("value", consumersJaxrs.getIntf().optional("value").get());
    Assert.assertFalse(consumersJaxrs.getIntf().optional(null).isPresent());
  }

  @Test
  public void completableFutureOptional_jaxrs_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("value", consumersJaxrs.getIntf().completableFutureOptional("value").get().get());
    Assert.assertFalse(consumersJaxrs.getIntf().completableFutureOptional(null).get().isPresent());
  }

  @Test
  public void optional_springmvc_intf() {
    Assert.assertEquals("value", consumersSpringmvc.getIntf().optional("value").get());
    Assert.assertFalse(consumersSpringmvc.getIntf().optional(null).isPresent());
  }

  @Test
  public void completableFutureOptional_springmvc_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("value", consumersSpringmvc.getIntf().completableFutureOptional("value").get().get());
    Assert.assertFalse(consumersSpringmvc.getIntf().completableFutureOptional(null).get().isPresent());
  }

  @Test
  public void responseEntityOptional_springmvc_intf() {
    Assert.assertEquals("value", consumersSpringmvc.getIntf().responseEntityOptional("value").getBody().get());
    Assert.assertFalse(consumersSpringmvc.getIntf().responseEntityOptional(null).getBody().isPresent());
  }

  @Test
  public void completableFutureResponseEntityOptional_springmvc_intf() throws ExecutionException, InterruptedException {
    Assert.assertEquals("value",
        consumersSpringmvc.getIntf().completableFutureResponseEntityOptional("value").get().getBody().get());
    Assert.assertFalse(
        consumersSpringmvc.getIntf().completableFutureResponseEntityOptional(null).get().getBody().isPresent());
  }
}
