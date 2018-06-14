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
import java.util.Collections;

import javax.xml.ws.Holder;

import org.apache.servicecomb.common.rest.RestEngineSchemaListener;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.SCBStatus;
import org.apache.servicecomb.core.unittest.UnitTestMeta;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class TestCseClientHttpRequest {
  static UnitTestMeta meta = new UnitTestMeta();

  @Before
  public void setup() {
    SCBEngine.getInstance().setStatus(SCBStatus.UP);
    CseContext.getInstance()
        .getSchemaListenerManager()
        .setSchemaListenerList(Collections.singletonList(new RestEngineSchemaListener()));
    meta.registerSchema(new SpringmvcSwaggerGeneratorContext(), SpringmvcImpl.class);
    SCBEngine.getInstance().setConsumerProviderManager(meta.getConsumerProviderManager());
  }

  @RequestMapping(path = "SpringmvcImpl")
  static class SpringmvcImpl {
    @RequestMapping(path = "/bytes", method = RequestMethod.POST)
    public byte[] bytes(@RequestBody byte[] input) {
      input[0] = (byte) (input[0] + 1);
      return input;
    }
  }

  @Test
  public void testNormal() {
    UnitTestMeta meta = new UnitTestMeta();

    CseContext.getInstance()
        .getSchemaListenerManager()
        .setSchemaListenerList(Collections.singletonList(new RestEngineSchemaListener()));

    meta.registerSchema(new SpringmvcSwaggerGeneratorContext(), SpringmvcImpl.class);

    Holder<Invocation> holder = new Holder<>();
    CseClientHttpRequest client =
        new CseClientHttpRequest(URI.create("cse://app:test/" + SpringmvcImpl.class.getSimpleName() + "/bytes"),
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
    client.setRequestBody(body);

    client.execute();

    Assert.assertArrayEquals(body, holder.value.getSwaggerArgument(0));
  }
}
