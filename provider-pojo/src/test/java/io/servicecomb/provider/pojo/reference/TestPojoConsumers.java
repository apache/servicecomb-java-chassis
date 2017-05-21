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

import io.servicecomb.provider.pojo.IPerson;
import io.servicecomb.provider.pojo.Person;
import io.servicecomb.provider.pojo.PersonReference;
import io.servicecomb.provider.pojo.reference.PojoConsumers;
import mockit.Injectable;

public class TestPojoConsumers {
    @Test
    public void testPojoConsumers(@Injectable ApplicationContext applicationContext) throws Exception {
        PersonReference bean = new PersonReference();

        PojoConsumers consumers = new PojoConsumers();
        consumers.processConsumerField(applicationContext, bean, bean.getClass().getField("person"));
        System.out.println(consumers.getConsumerList().get(0));
        Assert.assertEquals(consumers.getConsumerList().get(0).getObject() instanceof IPerson, true);
    }

    @Test
    public void testPojoConsumersNoReference(@Injectable ApplicationContext applicationContext) throws Exception {
        Person bean = new Person();
        PojoConsumers consumers = new PojoConsumers();
        consumers.processConsumerField(applicationContext, bean, bean.getClass().getField("name"));
        Assert.assertEquals(consumers.getConsumerList().size(), 0);
    }
}
