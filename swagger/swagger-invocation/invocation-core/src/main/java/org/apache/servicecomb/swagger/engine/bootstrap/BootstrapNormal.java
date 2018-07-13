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
package org.apache.servicecomb.swagger.engine.bootstrap;

import java.util.Arrays;

import org.apache.servicecomb.swagger.engine.SwaggerBootstrap;
import org.apache.servicecomb.swagger.engine.SwaggerEnvironment;
import org.apache.servicecomb.swagger.generator.core.CompositeSwaggerGeneratorContext;
import org.apache.servicecomb.swagger.invocation.arguments.consumer.ConsumerArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.consumer.ConsumerInvocationContextMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerArgumentsMapperFactory;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerInvocationContextMapperFactory;
import org.apache.servicecomb.swagger.invocation.converter.ConverterMgr;

public class BootstrapNormal implements SwaggerBootstrap {
  public SwaggerEnvironment boot() {
    SwaggerEnvironment env = new SwaggerEnvironment();

    ConverterMgr converterMgr = new ConverterMgr();

    ProducerArgumentsMapperFactory producerArgumentsFactory = new ProducerArgumentsMapperFactory();
    producerArgumentsFactory.setFactoryList(Arrays.asList(new ProducerInvocationContextMapperFactory()));
    producerArgumentsFactory.setConverterMgr(converterMgr);
    env.setProducerArgumentsFactory(producerArgumentsFactory);

    ConsumerArgumentsMapperFactory consumerArgumentsFactory = new ConsumerArgumentsMapperFactory();
    consumerArgumentsFactory.setFactoryList(Arrays.asList(new ConsumerInvocationContextMapperFactory()));
    consumerArgumentsFactory.setConverterMgr(converterMgr);
    env.setConsumerArgumentsFactory(consumerArgumentsFactory);

    env.setConverterMgr(converterMgr);
    env.setCompositeSwaggerGeneratorContext(new CompositeSwaggerGeneratorContext());

    return env;
  }
}
