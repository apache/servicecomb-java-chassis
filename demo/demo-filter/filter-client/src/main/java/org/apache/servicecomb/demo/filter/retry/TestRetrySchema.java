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

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestRetrySchema implements CategorizedTestCase {
  interface RetrySchemaInf {
    boolean successWhenRetry();

    CompletableFuture<Boolean> successWhenRetryAsync();
  }

  @RpcReference(microserviceName = "filterServer", schemaId = "RetrySchema")
  private RetrySchemaInf retrySchemaInf;

  RestTemplate restTemplate = RestTemplateBuilder.create();

  private static final String SERVER = "servicecomb://filterServer";

  @Override
  public void testAllTransport() throws Exception {
    testRetryGovernanceRestTemplate();
    testRetryGovernanceRpc();
  }

  private void testRetryGovernanceRpc() throws Exception {
    TestMgr.check(retrySchemaInf.successWhenRetry(), true);
    TestMgr.check(retrySchemaInf.successWhenRetry(), true);

    TestMgr.check(retrySchemaInf.successWhenRetryAsync().get(), true);
    TestMgr.check(retrySchemaInf.successWhenRetryAsync().get(), true);
  }

  private void testRetryGovernanceRestTemplate() {
    TestMgr.check(restTemplate.getForObject(SERVER + "/retry/governance/successWhenRetry", boolean.class), true);
    TestMgr.check(restTemplate.getForObject(SERVER + "/retry/governance/successWhenRetry", boolean.class), true);
  }
}
