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

package io.servicecomb.swagger.invocation.arguments.utils;

import java.util.Arrays;

import io.servicecomb.swagger.generator.core.SwaggerGenerator;
import io.servicecomb.swagger.generator.core.unittest.UnitTestSwaggerUtils;
import io.servicecomb.swagger.generator.core.utils.ClassUtils;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapper;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerInvocationContextMapperFactory;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapper;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerInvocationContextMapperFactory;

/**
 * <一句话功能简述>
 * <功能详细描述>
 *
 * @author   
 * @version  [版本号, 2017年4月14日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public class Meta {
    public OpMeta<ConsumerArgumentsMapper> consumerOpMeta = new OpMeta<>();

    public OpMeta<ProducerArgumentsMapper> producerOpMeta = new OpMeta<>();

    // 如果consumerIntf为null，表示等于契约接口
    public Meta(Class<?> consumerIntf, Class<?> producerCls) {
        // producer
        SwaggerGenerator producerGenerator = UnitTestSwaggerUtils.generateSwagger(producerCls);
        ProducerArgumentsMapperFactory producerFactory = new ProducerArgumentsMapperFactory();
        producerFactory.setFactoryList(Arrays.asList(new ProducerInvocationContextMapperFactory()));

        producerOpMeta.providerGenerator = producerGenerator;
        producerOpMeta.swagger = producerGenerator.getSwagger();
        producerOpMeta.factory = producerFactory;
        producerOpMeta.init();

        // consumer
        SwaggerGenerator consumerGenerator = null;
        if (consumerIntf == null) {
            consumerIntf = ClassUtils.getOrCreateInterface(producerGenerator);
        }
        consumerGenerator = UnitTestSwaggerUtils.generateSwagger(consumerIntf);
        ConsumerArgumentsMapperFactory consumerFactory = new ConsumerArgumentsMapperFactory();
        consumerFactory.setFactoryList(Arrays.asList(new ConsumerInvocationContextMapperFactory()));

        consumerOpMeta.providerGenerator = consumerGenerator;
        consumerOpMeta.swagger = producerGenerator.getSwagger();
        consumerOpMeta.factory = consumerFactory;
        consumerOpMeta.init();
    }
}
