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

package org.apache.servicecomb.demo.springmvc.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/*
This is the provider definition:

<code>
  @GetMapping(path = "/diffNames")
  @ApiOperation(value = "differentName", nickname = "differentName")
  public int diffNames(@RequestParam("x") int a, @RequestParam("y") int b)
</code>

and the swagger is:

<code>
  paths:
    /diffNames:
      get:
        summary: "differentName"
        operationId: "differentName"
        parameters:
        - name: "x"
          in: "query"
          required: true
          type: "integer"
          format: "int32"
        - name: "y"
          in: "query"
          required: true
          type: "integer"
          format: "int32"
        responses:
          "200":
            description: "response of 200"
            schema:
              type: "integer"
              format: "int32"
</code>

In consumer, you can define any prototype that matches generated swagger of provider.
 */

interface DiffNames {
  int differentName(int x, int y);
}

interface DiffNames2 {
  int differentName(int y, int x);
}

@Component
public class TestWeakSpringmvc implements CategorizedTestCase {
  @RpcReference(microserviceName = "springmvc", schemaId = "weakSpringmvc")
  private DiffNames diffNames;

  @RpcReference(microserviceName = "springmvc", schemaId = "weakSpringmvc")
  private DiffNames2 diffNames2;

  private RestTemplate restTemplate = RestTemplateBuilder.create();

  @Override
  public void testAllTransport() {
    TestMgr.check(7, diffNames.differentName(2, 3));
    TestMgr.check(8, diffNames2.differentName(2, 3));
    TestMgr.check(7, restTemplate.getForObject("cse://springmvc/weakSpringmvc/diffNames?x=2&y=3", Integer.class));
    Map<String, Object> args = new HashMap<>();
    args.put("x", 2);
    args.put("y", 3);
    TestMgr.check(7, InvokerUtils.syncInvoke("springmvc", "weakSpringmvc", "differentName", args));
  }
}
