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
package org.apache.servicecomb.provider.pojo.registry;

import java.util.Map.Entry;

import org.apache.servicecomb.core.registry.ThirdServiceRegister;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * <pre>
 * extend {@link ThirdServiceRegister} to register consumer client bean
 *
 * usage:
 *   1. declare the 3rd service
 *      {@code
 *        @Configuration
 *        public class Svc extends ThirdServiceWithInvokerRegister {
 *          public static final String NAME = "svc";
 *
 *          public Svc() {
 *            super(NAME);
 *
 *            addSchema("schema1", Schema1Client.class);
 *            addSchema("schema2", Schema2Client.class);
 *          }
 *        }
 *      }
 *   2. invoke the 3rd service same to normal servicecomb service
 *      {@code
 *        @Bean
 *        public class SomeService {
 *          private Schema1Client client;
 *
 *          @Autowired
 *          public void setClient(Schema1Client client) {
 *            this.client = client;
 *          }
 *
 *          public int add(int x, int y) {
 *            return client.add(x, y);
 *          }
 *        }
 *      }
 * </pre>
 */
public abstract class ThirdServiceWithInvokerRegister extends ThirdServiceRegister implements BeanFactoryPostProcessor {
  public ThirdServiceWithInvokerRegister(String microserviceName) {
    super(microserviceName);
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    for (Entry<String, Class<?>> entry : schemaByIdMap.entrySet()) {
      Object instance = createClientProxy(entry.getKey(), entry.getValue());
      beanFactory.registerSingleton(microserviceName + "_" + entry.getKey(), instance);
    }
  }

  protected Object createClientProxy(String schemaId, Class<?> consumerIntf) {
    return Invoker.createProxy(microserviceName, schemaId, consumerIntf);
  }
}
