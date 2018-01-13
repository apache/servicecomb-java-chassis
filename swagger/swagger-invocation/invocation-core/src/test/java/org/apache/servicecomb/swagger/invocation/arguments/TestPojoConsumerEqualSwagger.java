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

package org.apache.servicecomb.swagger.invocation.arguments;

import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.swagger.engine.SwaggerConsumer;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.engine.SwaggerProducer;
import org.apache.servicecomb.swagger.engine.bootstrap.BootstrapNormal;
import org.apache.servicecomb.swagger.engine.unittest.LocalProducerInvoker;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.models.JaxrsImpl;
import org.apache.servicecomb.swagger.invocation.models.Person;
import org.apache.servicecomb.swagger.invocation.models.PojoConsumerIntf;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPojoConsumerEqualSwagger {
  private static SwaggerEnvironment env;

  private static SwaggerProducer producer;

  private static SwaggerConsumer consumer;

  private static LocalProducerInvoker invoker;

  private static PojoConsumerIntf proxy;

  @BeforeClass
  public static void init() {
    env = new BootstrapNormal().boot();
    producer = env.createProducer(new JaxrsImpl());
    consumer = env.createConsumer(PojoConsumerIntf.class, producer.getSwaggerIntf());
    invoker = new LocalProducerInvoker(consumer, producer);
    proxy = invoker.getProxy();
  }

  @Test
  public void testSimple() throws Exception {
    int result = proxy.testSimple(1, 2, 3);

    Assert.assertEquals(1, (int) invoker.getSwaggerArgument(0));
    Assert.assertEquals(2, (int) invoker.getSwaggerArgument(1));
    Assert.assertEquals(3, (int) invoker.getSwaggerArgument(2));

    Assert.assertEquals(-4, result);
  }

  @Test
  public void testObject() throws Exception {
    Person person = new Person();
    person.setName("abc");

    Person result = proxy.testObject(person);

    Person swaggerPerson = invoker.getSwaggerArgument(0);
    Assert.assertEquals(person.getName(), swaggerPerson.getName());

    Assert.assertEquals("hello abc", result.getName());
  }

  @Test
  public void testSimpleAndObject() throws Exception {
    Person person = new Person();
    person.setName("abc");

    String result = proxy.testSimpleAndObject("prefix", person);

    Assert.assertEquals("prefix", (String) invoker.getSwaggerArgument(0));
    Assert.assertEquals(person.getName(), ((Person) invoker.getSwaggerArgument(1)).getName());

    Assert.assertEquals("prefix abc", result);
  }

  @Test
  public void testContext() throws Exception {
    InvocationContext threadContext = new InvocationContext();
    threadContext.addContext("ta", "tvalue");
    ContextUtils.setInvocationContext(threadContext);

    InvocationContext context = new InvocationContext();
    context.addContext("a", "value");

    String result = proxy.testContext(context, "name");

    Assert.assertEquals(3, invoker.getInvocation().getContext().size());
    Assert.assertEquals("tvalue", invoker.getContext("ta"));
    Assert.assertEquals("value", invoker.getContext("a"));
    Assert.assertEquals("name", invoker.getContext("name"));
    Assert.assertEquals("name", (String) invoker.getSwaggerArgument(0));

    Assert.assertEquals("name sayhi", result);
  }

  @Test
  public void testBytes() throws Exception {
    byte[] bytes = new byte[] {1, 2};

    byte[] result = proxy.testBytes(bytes);

    Assert.assertEquals(bytes, invoker.getSwaggerArgument(0));

    Assert.assertArrayEquals(bytes, (byte[]) result);
  }

  @Test
  public void testArrayArray() throws Exception {
    String[] array = new String[] {"a", "b"};
    List<String> list = Arrays.asList(array);

    String[] result = proxy.testArrayArray(array);

    Assert.assertEquals(list, (List<?>) invoker.getSwaggerArgument(0));

    Assert.assertArrayEquals(array, (String[]) result);
  }

  @Test
  public void testArrayList() throws Exception {
    String[] array = new String[] {"a", "b"};
    List<String> list = Arrays.asList(array);
    List<String> result = proxy.testArrayList(array);

    Assert.assertEquals(list, invoker.getSwaggerArgument(0));

    Assert.assertEquals(list, result);
  }

  @Test
  public void testListArray() throws Exception {
    String[] array = new String[] {"a", "b"};
    List<String> list = Arrays.asList(array);

    String[] result = proxy.testListArray(list);

    Assert.assertEquals(list, invoker.getSwaggerArgument(0));

    Assert.assertArrayEquals(array, result);
  }

  @Test
  public void testListList() throws Exception {
    List<String> list = Arrays.asList("a", "b");

    List<String> result = proxy.testListList(list);

    Assert.assertEquals(list, invoker.getSwaggerArgument(0));

    Assert.assertEquals(list, result);
  }

  @Test
  public void testObjectArrayArray() throws Exception {
    Person[] array = new Person[] {new Person("a"), new Person("b")};
    List<Person> list = Arrays.asList(array);

    Person[] result = proxy.testObjectArrayArray(array);

    Assert.assertEquals(list, (List<?>) invoker.getSwaggerArgument(0));

    Assert.assertArrayEquals(array, (Person[]) result);
  }

  @Test
  public void testObjectArrayList() throws Exception {
    Person[] array = new Person[] {new Person("a"), new Person("b")};
    List<Person> list = Arrays.asList(array);
    List<Person> result = proxy.testObjectArrayList(array);

    Assert.assertEquals(list, invoker.getSwaggerArgument(0));

    Assert.assertEquals(list, result);
  }

  @Test
  public void testObjectListArray() throws Exception {
    Person[] array = new Person[] {new Person("a"), new Person("b")};
    List<Person> list = Arrays.asList(array);

    Person[] result = proxy.testObjectListArray(list);

    Assert.assertEquals(list, invoker.getSwaggerArgument(0));
    Assert.assertEquals(list, invoker.getProducerResponse().getResult());

    Assert.assertArrayEquals(array, result);
  }

  @Test
  public void testObjectListList() throws Exception {
    Person[] array = new Person[] {new Person("a"), new Person("b")};
    List<Person> list = Arrays.asList(array);

    List<Person> result = proxy.testObjectListList(list);

    Assert.assertEquals(list, invoker.getSwaggerArgument(0));

    Assert.assertEquals(list, result);
  }
}
