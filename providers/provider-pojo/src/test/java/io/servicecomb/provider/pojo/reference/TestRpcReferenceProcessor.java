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

package io.servicecomb.provider.pojo.reference;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import io.servicecomb.provider.pojo.Person;
import io.servicecomb.provider.pojo.PersonReference;
import mockit.Injectable;

public class TestRpcReferenceProcessor {
    @Test
    public void testReference(@Injectable ApplicationContext applicationContext) throws Exception {
        PersonReference bean = new PersonReference();

        Assert.assertNull(bean.person);

        RpcReferenceProcessor consumers = new RpcReferenceProcessor();
        consumers.setEmbeddedValueResolver((strVal) -> strVal);
        consumers.processConsumerField(applicationContext, bean, bean.getClass().getField("person"));

        Assert.assertNotNull(bean.person);
    }

    @Test
    public void testNoReference(@Injectable ApplicationContext applicationContext) throws Exception {
        Person bean = new Person();

        Assert.assertNull(bean.name);

        RpcReferenceProcessor consumers = new RpcReferenceProcessor();
        consumers.processConsumerField(applicationContext, bean, bean.getClass().getField("name"));

        Assert.assertNull(bean.name);
    }
}
