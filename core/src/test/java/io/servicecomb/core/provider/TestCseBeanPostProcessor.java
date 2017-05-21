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

package io.servicecomb.core.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import io.servicecomb.core.provider.CseBeanPostProcessor;
import io.servicecomb.core.provider.CseBeanPostProcessor.ConsumerFieldProcessor;
import io.servicecomb.core.provider.CseBeanPostProcessor.ProviderProcessor;
import mockit.Deencapsulation;
import mockit.Injectable;

public class TestCseBeanPostProcessor {
    class MyProviderProcessor implements CseBeanPostProcessor.ProviderProcessor {
        @Override
        public void processProvider(ApplicationContext applicationContext, String beanName, Object bean) {
            this.postProcessBeforeInitialization(bean, beanName);
            this.postProcessAfterInitialization(bean, beanName);
            Assert.assertEquals(beanName, "test");
        }
    }

    class MyConsumerFieldProcessor implements CseBeanPostProcessor.ConsumerFieldProcessor {
        @Override
        public <CONSUMER_ANNOTATION> void processConsumerField(ApplicationContext applicationContext, Object bean,
                Field field) {
        }
    }

    @Test
    public void testCseBeanPostProcessor(@Injectable ApplicationContext context) {
        CseBeanPostProcessor processor = new CseBeanPostProcessor();
        processor.setApplicationContext(context);
        List<ProviderProcessor> providerProcessor = new ArrayList<>();
        providerProcessor.add(new MyProviderProcessor());

        List<ConsumerFieldProcessor> consumerProcessor = new ArrayList<>();
        consumerProcessor.add(new MyConsumerFieldProcessor());

        Deencapsulation.setField(processor, "providerProcessorList", providerProcessor);
        Deencapsulation.setField(processor, "consumerProcessorList", consumerProcessor);

        processor.postProcessBeforeInitialization(new Person(), "test");
        processor.postProcessAfterInitialization(new Person(), "test");
    }

    @Test
    public void testCseBeanPostProcessorListNull(@Injectable ApplicationContext context) {
        CseBeanPostProcessor processor = new CseBeanPostProcessor();
        processor.setApplicationContext(context);
        List<ProviderProcessor> providerProcessor = new ArrayList<>();

        List<ConsumerFieldProcessor> consumerProcessor = new ArrayList<>();

        Deencapsulation.setField(processor, "providerProcessorList", providerProcessor);
        Deencapsulation.setField(processor, "consumerProcessorList", consumerProcessor);

        processor.postProcessBeforeInitialization(new Person(), "test");
        processor.postProcessAfterInitialization(new Person(), "test");
    }
}
