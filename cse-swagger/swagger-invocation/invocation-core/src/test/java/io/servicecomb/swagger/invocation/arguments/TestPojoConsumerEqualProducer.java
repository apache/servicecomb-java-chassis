/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.swagger.invocation.arguments;

import java.util.Arrays;
import java.util.List;

import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapper;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import io.servicecomb.swagger.invocation.arguments.utils.Utils;
import io.servicecomb.swagger.invocation.models.PojoConsumerIntf;
import io.servicecomb.swagger.invocation.models.PojoImpl;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.swagger.invocation.SwaggerInvocation;
import io.servicecomb.swagger.invocation.SwaggerInvocationContext;
import io.servicecomb.swagger.invocation.arguments.utils.Meta;
import io.servicecomb.swagger.invocation.models.Person;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 *
 * @version  [版本号, 2017年4月15日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class TestPojoConsumerEqualProducer {
    // consumer接口原型等于producer，不等于契约
    Meta meta = new Meta(PojoConsumerIntf.class, PojoImpl.class);

    ConsumerArgumentsMapper consumerMapper;

    ProducerArgumentsMapper producerMapper;

    SwaggerInvocation invocation = new SwaggerInvocation();

    protected void prepare(String methodName) {
        consumerMapper = meta.consumerOpMeta.findArgsMapper(methodName);
        producerMapper = meta.producerOpMeta.findArgsMapper(methodName);
    }

    @Test
    public void testTwoSimple() throws Exception {
        prepare("testTwoSimple");
        consumerMapper.toInvocation(new Object[] {1, 2}, invocation);

        Object body = invocation.getSwaggerArgument(0);
        Assert.assertEquals(1, Utils.getFieldValue(body, "a"));
        Assert.assertEquals(2, Utils.getFieldValue(body, "b"));

        Object[] producerArgs = producerMapper.toProducerArgs(invocation);
        Assert.assertEquals(1, producerArgs[0]);
        Assert.assertEquals(2, producerArgs[1]);
    }

    @Test
    public void testObject() throws Exception {
        prepare("testObject");

        Person person = new Person();
        person.setName("abc");
        consumerMapper.toInvocation(new Object[] {person}, invocation);

        Person swaggerPerson = invocation.getSwaggerArgument(0);
        Assert.assertEquals(person, swaggerPerson);

        Object[] producerArgs = producerMapper.toProducerArgs(invocation);
        Assert.assertEquals(person, producerArgs[0]);
    }

    @Test
    public void testSimpleAndObject() throws Exception {
        prepare("testSimpleAndObject");

        Person person = new Person();
        person.setName("abc");
        consumerMapper.toInvocation(new Object[] {"prefix", person}, invocation);

        Object body = invocation.getSwaggerArgument(0);
        Assert.assertEquals("prefix", Utils.getFieldValue(body, "prefix"));
        Assert.assertEquals(person, Utils.getFieldValue(body, "user"));

        Object[] producerArgs = producerMapper.toProducerArgs(invocation);
        Assert.assertEquals("prefix", producerArgs[0]);
        Assert.assertEquals(person, producerArgs[1]);
    }

    @Test
    public void testContext() throws Exception {
        prepare("testContext");

        SwaggerInvocationContext context = new SwaggerInvocationContext();
        context.addContext("a", "value");
        consumerMapper.toInvocation(new Object[] {context, "name"}, invocation);

        Object body = invocation.getSwaggerArgument(0);
        Assert.assertEquals("value", invocation.getContext("a"));
        Assert.assertEquals("name", Utils.getFieldValue(body, "name"));

        Object[] producerArgs = producerMapper.toProducerArgs(invocation);
        Assert.assertEquals(context.getContext(), ((SwaggerInvocationContext) producerArgs[0]).getContext());
        Assert.assertEquals("name", producerArgs[1]);
    }

    @Test
    public void testList() throws Exception {
        prepare("testList");

        List<String> list = Arrays.asList("a", "b");
        consumerMapper.toInvocation(new Object[] {list}, invocation);

        Object body = invocation.getSwaggerArgument(0);
        Assert.assertEquals(list, Utils.getFieldValue(body, "s"));

        Object[] producerArgs = producerMapper.toProducerArgs(invocation);
        Assert.assertEquals(list, producerArgs[0]);
    }
}
