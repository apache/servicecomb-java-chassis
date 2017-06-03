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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import io.servicecomb.swagger.invocation.SwaggerInvocation;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapper;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import io.servicecomb.swagger.invocation.arguments.utils.Meta;
import io.servicecomb.swagger.invocation.arguments.utils.Utils;
import io.servicecomb.swagger.invocation.models.PojoImpl;
import org.junit.Assert;
import org.junit.Test;

import io.servicecomb.swagger.invocation.SwaggerInvocationContext;
import io.servicecomb.swagger.invocation.models.Person;
import io.servicecomb.foundation.common.utils.ReflectUtils;

public class TestPojoConsumerEqualSwagger {
    // consumer接口原型等于契约，不等于producer
    Meta meta = new Meta(null, PojoImpl.class);

    ConsumerArgumentsMapper consumerMapper;

    Method swaggerMethod;

    ProducerArgumentsMapper producerMapper;

    SwaggerInvocation invocation = new SwaggerInvocation();

    protected void prepare(String methodName) {
        consumerMapper = meta.consumerOpMeta.findArgsMapper(methodName);
        swaggerMethod = meta.consumerOpMeta.findSwaggerMethod(methodName);
        producerMapper = meta.producerOpMeta.findArgsMapper(methodName);
    }

    protected Object createBodyInstance(int idx) {
        try {
            return swaggerMethod.getParameterTypes()[idx].newInstance();
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Test
    public void testTwoSimple() throws Exception {
        prepare("testTwoSimple");

        Object body = createBodyInstance(0);
        ReflectUtils.setField(body, "a", 1);
        ReflectUtils.setField(body, "b", 2);

        consumerMapper.toInvocation(new Object[] {body}, invocation);

        Assert.assertEquals(body, invocation.getSwaggerArgument(0));

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

        Object body = createBodyInstance(0);
        ReflectUtils.setField(body, "prefix", "prefix");
        ReflectUtils.setField(body, "user", person);

        consumerMapper.toInvocation(new Object[] {body}, invocation);

        Assert.assertEquals(body, invocation.getSwaggerArguments()[0]);

        Object[] producerArgs = producerMapper.toProducerArgs(invocation);
        Assert.assertEquals("prefix", producerArgs[0]);
        Assert.assertEquals(person, producerArgs[1]);
    }

    @Test
    public void testContext() throws Exception {
        prepare("testContext");

        Object body = createBodyInstance(0);
        ReflectUtils.setField(body, "name", "name");
        
        consumerMapper.toInvocation(new Object[] {body}, invocation);
        
        Assert.assertEquals(true, invocation.getContext().isEmpty());
        Assert.assertEquals(body, invocation.getSwaggerArgument(0));

        Object[] producerArgs = producerMapper.toProducerArgs(invocation);
        Assert.assertEquals(true, ((SwaggerInvocationContext) producerArgs[0]).getContext().isEmpty());
        Assert.assertEquals("name", producerArgs[1]);
    }

    @Test
    public void testList() throws Exception {
        prepare("testList");

        List<String> list = Arrays.asList("a", "b");
        
        Object body = createBodyInstance(0);
        ReflectUtils.setField(body, "s", list);
        consumerMapper.toInvocation(new Object[] {body}, invocation);

        Assert.assertEquals(list, Utils.getFieldValue(body, "s"));

        Object[] producerArgs = producerMapper.toProducerArgs(invocation);
        Assert.assertEquals(list, producerArgs[0]);
    }
}
