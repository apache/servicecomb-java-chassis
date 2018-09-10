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
import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.vertx.VertxUtils;
import org.apache.servicecomb.serviceregistry.RegistryUtils;
import org.apache.servicecomb.serviceregistry.registry.AbstractServiceRegistry;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;

import com.netflix.config.DynamicProperty;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class TestAbstractTransport {
  private Method updatePropertyMethod =
      ReflectionUtils.findMethod(DynamicProperty.class, "updateProperty", String.class, Object.class);

  private void updateProperty(String key, Object value) {
    updatePropertyMethod.setAccessible(true);
    ReflectionUtils.invokeMethod(updatePropertyMethod, null, key, value);
  }

  class MyAbstractTransport extends AbstractTransport {

    @Override
    public String getName() {
      return "my";
    }

    @Override
    public boolean init() throws Exception {
      return true;
    }

    @Override
    public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    }
  }

  @Injectable
  AbstractServiceRegistry serviceRegistry;

  @Before
  public void setup() {
    RegistryUtils.setServiceRegistry(serviceRegistry);
  }

  @After
  public void teardown() {
    RegistryUtils.setServiceRegistry(null);
  }

  @AfterClass
  public static void classTeardown() {
    VertxUtils.closeVertxByName("transport");
  }

  @Test
  public void testSetListenAddressWithoutSchemaChineseSpaceNewSC() throws UnsupportedEncodingException {
    new Expectations() {
      {
        serviceRegistry.getFeatures().isCanEncodeEndpoint();
        result = true;
      }
    };

    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setListenAddressWithoutSchema("127.0.0.1:9090", Collections.singletonMap("country", "中 国"));
    Assert.assertEquals("my://127.0.0.1:9090?country=" + URLEncoder.encode("中 国", StandardCharsets.UTF_8.name()),
        transport.getEndpoint().getEndpoint());
  }

  @Test
  public void testSetListenAddressWithoutSchemaChineseSpaceOldSC() throws UnsupportedEncodingException {
    MyAbstractTransport transport = new MyAbstractTransport();
    try {
      transport.setListenAddressWithoutSchema("127.0.0.1:9090", Collections.singletonMap("country", "中 国"));
      Assert.fail("must throw exception");
    } catch (ServiceCombException e) {
      Assert.assertEquals(
          "current service center not support encoded endpoint, please do not use chinese or space or anything need to be encoded.",
          e.getMessage());
      Assert.assertEquals(
          "Illegal character in query at index 31: rest://127.0.0.1:9090?country=中 国",
          e.getCause().getMessage());
    }
  }

  @Test
  public void testSetListenAddressWithoutSchemaNormalNotEncode() throws UnsupportedEncodingException {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setListenAddressWithoutSchema("127.0.0.1:9090", Collections.singletonMap("country", "chinese"));
    Assert.assertEquals("my://127.0.0.1:9090?country=chinese", transport.getEndpoint().getEndpoint());
  }

  @Test
  public void testSetListenAddressWithoutSchemaAlreadyHaveQuery() throws UnsupportedEncodingException {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setListenAddressWithoutSchema("127.0.0.1:9090?a=aValue",
        Collections.singletonMap("country", "chinese"));
    Assert.assertEquals("my://127.0.0.1:9090?a=aValue&country=chinese", transport.getEndpoint().getEndpoint());
  }

  @Test
  public void testMyAbstractTransport() throws Exception {
    MyAbstractTransport transport = new MyAbstractTransport();
    transport.setListenAddressWithoutSchema("127.0.0.1:9090");
    Assert.assertEquals("my", transport.getName());
    Assert.assertEquals("my://127.0.0.1:9090", transport.getEndpoint().getEndpoint());
    Assert.assertEquals("127.0.0.1", ((IpPort) transport.parseAddress("my://127.0.0.1:9090")).getHostOrIp());
    transport.setListenAddressWithoutSchema("0.0.0.0:9090");
    Assert.assertNotEquals("my://127.0.0.1:9090", transport.getEndpoint().getEndpoint());
    transport.setListenAddressWithoutSchema(null);
    Assert.assertNull(transport.getEndpoint().getEndpoint());
    Assert.assertNull(transport.parseAddress(null));
    Assert.assertEquals(30000, AbstractTransport.getReqTimeout("sayHi", "hello", "test"));
  }

  @Test(expected = NumberFormatException.class)
  public void testMyAbstractTransportException(@Mocked TransportManager manager) throws Exception {
    MyAbstractTransport transport = new MyAbstractTransport();

    transport.setListenAddressWithoutSchema(":127.0.0.1:9090");
  }

  /**
   * Tests the request call timeout for service level timeout value
   */
  @Test
  public void testRequestCfgService() throws Exception {
    System.setProperty("servicecomb.request.hello1.timeout", "3000");
    //check for service level timeout value
    Assert.assertEquals(3000, AbstractTransport.getReqTimeout("sayHello1", "sayHelloSchema1", "hello1"));
    System.getProperties().remove("servicecomb.request.hello1.timeout");
  }

  /**
   * Tests the request call timeout for schema level timeout value
   */
  @Test
  public void testRequestCfgSchema() throws Exception {
    System.setProperty("servicecomb.request.hello2.sayHelloSchema2.timeout", "2000");

    Assert.assertEquals(2000, AbstractTransport.getReqTimeout("sayHello2", "sayHelloSchema2", "hello2"));
    System.getProperties().remove("servicecomb.request.hello2.sayHelloSchema2.timeout");
  }

  /**
   * Tests the request call timeout for operatation level timeout value
   */
  @Test
  public void testRequestCfgOperation() throws Exception {
    System.setProperty("servicecomb.request.hello3.sayHelloSchema3.sayHello3.timeout", "1000");

    Assert.assertEquals(1000, AbstractTransport.getReqTimeout("sayHello3", "sayHelloSchema3", "hello3"));
    System.getProperties().remove("servicecomb.request.hello3.sayHelloSchema3.sayHello3.timeout");
  }

  /**
   * Tests the request call timeout with configuration change event for operation level config.
   */
  @Test
  public void testRequestTimeoutCfgEvent() {
    System.setProperty("servicecomb.request.hello4.sayHelloSchema4.sayHello4.timeout", "1000");
    Invocation invocation = Mockito.mock(Invocation.class);
    Mockito.when(invocation.getOperationName()).thenReturn("sayHello4");
    Mockito.when(invocation.getSchemaId()).thenReturn("sayHelloSchema4");
    Mockito.when(invocation.getMicroserviceName()).thenReturn("hello4");
    Assert.assertEquals(1000, AbstractTransport.getReqTimeout("sayHello4", "sayHelloSchema4", "hello4"));

    updateProperty("servicecomb.request.hello4.sayHelloSchema4.sayHello4.timeout", 2000);

    Assert.assertEquals(2000, AbstractTransport.getReqTimeout("sayHello4", "sayHelloSchema4", "hello4"));
    System.getProperties().remove("servicecomb.request.hello4.sayHelloSchema4.sayHello4.timeout");
  }
}
