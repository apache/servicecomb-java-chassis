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
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @version  [版本号, 2017年4月28日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component
public class CseBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor {
    private ApplicationContext applicationContext;

    public interface EmptyBeanPostProcessor extends BeanPostProcessor {
        default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }
    }

    public interface ProviderProcessor extends EmptyBeanPostProcessor {
        void processProvider(ApplicationContext applicationContext, String beanName, Object bean);
    }

    public interface ConsumerFieldProcessor extends EmptyBeanPostProcessor {
        <CONSUMER_ANNOTATION> void processConsumerField(ApplicationContext applicationContext, Object bean,
                Field field);
    }

    @Autowired(required = false)
    private List<ProviderProcessor> providerProcessorList;

    @Autowired(required = false)
    private List<ConsumerFieldProcessor> consumerProcessorList;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (consumerProcessorList == null || consumerProcessorList.isEmpty()) {
            return bean;
        }
        // 扫描所有field，处理扩展的field标注
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                for (ConsumerFieldProcessor processor : consumerProcessorList) {
                    processor.processConsumerField(applicationContext, bean, field);
                }
            }
        });
        return bean;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (providerProcessorList != null && !providerProcessorList.isEmpty()) {
            for (ProviderProcessor processor : providerProcessorList) {
                processor.processProvider(applicationContext, beanName, bean);
            }
        }

        return bean;
    }
}
