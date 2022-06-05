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

package org.apache.servicecomb.core.transport;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.registry.RegistrationManager;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.util.ReflectionUtils;

import com.netflix.config.DynamicProperty;

import mockit.Expectations;
import mockit.Mocked;

public class TestAbstractTransport {
  private final Method updatePropertyMethod =
      ReflectionUtils.findMethod(DynamicProperty.class, "updateProperty", String.class, Object.class);

  private void updateProperty(String key, Object value) {
    updatePropertyMethod.setAccessible(true);
    ReflectionUtils.invokeMethod(updatePropertyMethod, null, key, value);
  }

  static class MyAbstractTransport extends AbstractTransport {

    @Override
    public String getName() {
      return "my";
    }

    @Override
    public boolean init() {
      return true;
    }

    @Override
    public void send(Invocation invocation, AsyncResponse asyncResp) {
    }
  }

  @AfterClass
  public static void classTeardown() {
    VertxUtils.blockCloseVertxByName("transport");
  }

  @Test
  public void testSetListenAddressWithoutSchemaChineseSpaceNewSC() throws UnsupportedEncodingException {
    new Expectations() {
      {
        RegistrationManager.getPublishAddress("my", "127.0.0.1:9090");
      }
    };

    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setListenAddressWithoutSchema("127.0.0.1:9090", Collections.singletonMap("country", "中 国"));
    Assertions.assertEquals("my://127.0.0.1:9090?country=" + URLEncoder.encode("中 国", StandardCharsets.UTF_8.name()),
        transport.getEndpoint().getEndpoint());
  }

  @Test
  public void testSetListenAddressWithoutSchemaNormalNotEncode() {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setListenAddressWithoutSchema("127.0.0.1:9090", Collections.singletonMap("country", "chinese"));
    Assertions.assertEquals("my://127.0.0.1:9090?country=chinese", transport.getEndpoint().getEndpoint());
  }

  @Test
  public void testSetListenAddressWithoutSchemaAlreadyHaveQuery() {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setListenAddressWithoutSchema("127.0.0.1:9090?a=aValue",
        Collections.singletonMap("country", "chinese"));
    Assertions.assertEquals("my://127.0.0.1:9090?a=aValue&country=chinese", transport.getEndpoint().getEndpoint());
  }

  @Test
  public void testMyAbstractTransport() {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setListenAddressWithoutSchema("127.0.0.1:9090");
    Assertions.assertEquals("my", transport.getName());
    Assertions.assertEquals("my://127.0.0.1:9090", transport.getEndpoint().getEndpoint());
    Assertions.assertEquals("127.0.0.1", ((IpPort) transport.parseAddress("my://127.0.0.1:9090")).getHostOrIp());
    transport.setListenAddressWithoutSchema("0.0.0.0:9090");
    Assertions.assertNotEquals("my://127.0.0.1:9090", transport.getEndpoint().getEndpoint());
    transport.setListenAddressWithoutSchema(null);
    Assertions.assertNull(transport.getEndpoint().getEndpoint());
    Assertions.assertNull(transport.parseAddress(null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMyAbstractTransportException(@Mocked TransportManager manager) {
    MyAbstractTransport transport = new MyAbstractTransport();

    transport.setListenAddressWithoutSchema(":127.0.0.1:9090");
  }
}
