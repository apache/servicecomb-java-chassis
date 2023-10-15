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

import static org.apache.servicecomb.core.transport.AbstractTransport.PUBLISH_ADDRESS;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import io.vertx.core.file.impl.FileResolverImpl;

public class TestAbstractTransport {
  Environment environment = Mockito.mock(Environment.class);

  static class MyAbstractTransport extends AbstractTransport {

    @Override
    public String getName() {
      return "my";
    }

    @Override
    public boolean init() {
      return true;
    }
  }

  @BeforeEach
  public void setUp() {
    Mockito.when(environment.getProperty(PUBLISH_ADDRESS, "")).thenReturn("");
    Mockito.when(environment.getProperty("servicecomb.my.publishPort", int.class, 0)).thenReturn(0);
    Mockito.when(environment.getProperty("servicecomb.transport.eventloop.size", int.class, -1))
        .thenReturn(-1);
    Mockito.when(environment.getProperty(FileResolverImpl.DISABLE_CP_RESOLVING_PROP_NAME, boolean.class, true))
        .thenReturn(true);
    LegacyPropertyFactory.setEnvironment(environment);
  }

  @Test
  public void testSetListenAddressWithoutSchemaChineseSpaceNewSC() throws UnsupportedEncodingException {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setEnvironment(environment);
    transport.setListenAddressWithoutSchema("127.0.0.1:9090", Collections.singletonMap("country", "中 国"));
    Assertions.assertEquals("my://127.0.0.1:9090?country=" + URLEncoder.encode("中 国", StandardCharsets.UTF_8.name()),
        transport.getEndpoint().getEndpoint());
  }

  @Test
  public void testSetListenAddressWithoutSchemaNormalNotEncode() {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setEnvironment(environment);
    transport.setListenAddressWithoutSchema("127.0.0.1:9090", Collections.singletonMap("country", "chinese"));
    Assertions.assertEquals("my://127.0.0.1:9090?country=chinese", transport.getEndpoint().getEndpoint());
  }

  @Test
  public void testSetListenAddressWithoutSchemaAlreadyHaveQuery() {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setEnvironment(environment);
    transport.setListenAddressWithoutSchema("127.0.0.1:9090?a=aValue",
        Collections.singletonMap("country", "chinese"));
    Assertions.assertEquals("my://127.0.0.1:9090?a=aValue&country=chinese", transport.getEndpoint().getEndpoint());
  }

  @Test
  public void testMyAbstractTransport() {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setEnvironment(environment);
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

  @Test
  public void testMyAbstractTransportException() {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setEnvironment(environment);
    Assertions.assertThrows(IllegalArgumentException.class, () ->
        transport.setListenAddressWithoutSchema(":127.0.0.1:9090"));
  }
}
