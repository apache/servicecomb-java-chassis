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
package io.servicecomb.swagger.engine.bootstrap;

import java.util.Arrays;

import io.servicecomb.swagger.engine.SwaggerBootstrap;
import io.servicecomb.swagger.engine.SwaggerEnvironment;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.arguments.consumer.ConsumerInvocationContextMapperFactory;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import io.servicecomb.swagger.invocation.arguments.producer.ProducerInvocationContextMapperFactory;
import io.servicecomb.swagger.invocation.converter.ConverterMgr;
import io.servicecomb.swagger.invocation.response.consumer.ConsumerResponseMapperFactory;
import io.servicecomb.swagger.invocation.response.producer.ProducerResponseMapperFactory;

public class BootstrapNormal implements SwaggerBootstrap {
  public SwaggerEnvironment boot() {
    SwaggerEnvironment env = new SwaggerEnvironment();

    ConverterMgr converterMgr = new ConverterMgr();

    ProducerArgumentsMapperFactory producerArgumentsFactory = new ProducerArgumentsMapperFactory();
    producerArgumentsFactory.setFactoryList(Arrays.asList(new ProducerInvocationContextMapperFactory()));
    producerArgumentsFactory.setConverterMgr(converterMgr);
    env.setProducerArgumentsFactory(producerArgumentsFactory);

    ProducerResponseMapperFactory producerResponseMapperFactory = new ProducerResponseMapperFactory();
    producerResponseMapperFactory.setConverterMgr(converterMgr);
    env.setProducerResponseMapperFactory(producerResponseMapperFactory);

    ConsumerArgumentsMapperFactory consumerArgumentsFactory = new ConsumerArgumentsMapperFactory();
    consumerArgumentsFactory.setFactoryList(Arrays.asList(new ConsumerInvocationContextMapperFactory()));
    consumerArgumentsFactory.setConverterMgr(converterMgr);
    env.setConsumerArgumentsFactory(consumerArgumentsFactory);

    ConsumerResponseMapperFactory consumerResponseMapperFactory = new ConsumerResponseMapperFactory();
    consumerResponseMapperFactory.setConverterMgr(converterMgr);
    env.setConsumerResponseMapperFactory(consumerResponseMapperFactory);

    return env;
  }
}
