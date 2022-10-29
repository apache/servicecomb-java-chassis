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
package org.apache.servicecomb.provider.springmvc.reference;

import java.net.URI;

import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.apache.servicecomb.registry.DiscoveryManager;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class TestCseClientHttpRequest {
  static SCBEngine scbEngine;

  @BeforeAll
  public static void classSetup() {
    ConfigUtil.installDynamicConfig();

    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new SpringmvcImpl()).run();
  }

  @AfterAll
  public static void classTeardown() {
    scbEngine.destroy();
    DiscoveryManager.renewInstance();
    ArchaiusUtils.resetConfig();
  }

  @RequestMapping(path = "SpringmvcImpl")
  static class SpringmvcImpl {
    @RequestMapping(path = "/bytes", method = RequestMethod.POST)
    public byte[] bytes(@RequestBody byte[] input, @RequestHeader String token) {
      input[0] = (byte) (input[0] + 1);
      return input;
    }
  }

  @Test
  public void testNormal() {
    Holder<Invocation> holder = new Holder<>();
    CseClientHttpRequest client =
        new CseClientHttpRequest(
            URI.create("cse://defaultMicroservice/" + SpringmvcImpl.class.getSimpleName() + "/bytes"),
            HttpMethod.POST) {

          /**
           * {@inheritDoc}
           */
          @Override
          protected Response doInvoke(Invocation invocation) {
            holder.value = invocation;
            return Response.ok("result");
          }
        };
    byte[] body = "abc".getBytes();
    HttpHeaders headers = new HttpHeaders();
    headers.add("token", "123");
    client.setRequestBody(body);
    client.setHttpHeaders(headers);

    client.execute();

    Assertions.assertArrayEquals(body, (byte[]) holder.value.getInvocationArguments().get("input"));
    Assertions.assertEquals("123", holder.value.getInvocationArguments().get("token"));
  }
}
