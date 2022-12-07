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

package org.apache.servicecomb.demo.jaxrs.client;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestQueryParamSchema implements CategorizedTestCase {
  public interface QueryParamService {
    String testQueryEncode(String param);
  }

  private final RestTemplate restTemplate = RestTemplateBuilder.create();

  @RpcReference(microserviceName = "jaxrs", schemaId = "QueryParamSchema")
  private QueryParamService queryParamService;

  @Override
  public void testAllTransport() throws Exception {
    testQueryParamEncodingPlus();
  }

  private void testQueryParamEncodingPlus() {
    // should encode +
    TestMgr.check("a+b",
        restTemplate.getForObject("servicecomb://jaxrs/queryParam/testQueryEncode?param={1}", String.class, "a+b"));
    // if not encoded, + should be blank
    TestMgr.check("a b",
        restTemplate.getForObject("servicecomb://jaxrs/queryParam/testQueryEncode?param=a+b", String.class));

    // rpc should encode
    TestMgr.check("a+b", queryParamService.testQueryEncode("a+b"));
  }
}
