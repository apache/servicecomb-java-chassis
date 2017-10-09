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
package io.servicecomb.provider.rest.common;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import io.servicecomb.core.provider.producer.ProducerMeta;
import io.servicecomb.foundation.common.utils.BeanUtils;

@Component
public class RestProducers implements BeanPostProcessor {
  private List<ProducerMeta> producerMetaList = new ArrayList<>();

  public List<ProducerMeta> getProducerMetaList() {
    return producerMetaList;
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
    RestSchema restSchema = beanCls.getAnnotation(RestSchema.class);
    if (restSchema == null) {
      return;
    }

    ProducerMeta producerMeta = new ProducerMeta(restSchema.schemaId(), bean, beanCls);
    producerMetaList.add(producerMeta);
  }
}
