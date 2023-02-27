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
package org.apache.servicecomb.provider.pojo.schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.provider.producer.ProducerMeta;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.provider.pojo.RpcSchema;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class PojoProducers implements BeanPostProcessor {
  private final List<ProducerMeta> producerMetas = new ArrayList<>();

  public synchronized void registerPojoProducer(PojoProducerMeta pojoProducer) {
    producerMetas.add(pojoProducer);
  }

  public List<ProducerMeta> getProducerMetas() {
    return producerMetas;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    processProvider(beanName, bean);

    return bean;
  }

  protected void processProvider(String beanName, Object bean) {
    // aop后，新的实例的父类可能是原class，也可能只是个proxy，父类不是原class
    // 所以，需要先取出原class，再取标注
    Class<?> beanCls = BeanUtils.getImplClassFromBean(bean);
    if (beanCls == null) {
      return;
    }
    RpcSchema rpcSchema = beanCls.getAnnotation(RpcSchema.class);
    if (rpcSchema == null) {
      return;
    }

    String schemaId = rpcSchema.schemaId();
    if (StringUtils.isEmpty(schemaId)) {
      Class<?>[] intfs = beanCls.getInterfaces();
      if (intfs.length == 1) {
        schemaId = intfs[0].getName();
      } else {
        throw new Error("Must be schemaId or implements only one interface");
      }
    }

    PojoProducerMeta pojoProducerMeta = new PojoProducerMeta();
    pojoProducerMeta.setSchemaId(schemaId);
    pojoProducerMeta.setSchemaInterface(rpcSchema.schemaInterface());
    pojoProducerMeta.setInstance(bean);

    registerPojoProducer(pojoProducerMeta);
  }
}
