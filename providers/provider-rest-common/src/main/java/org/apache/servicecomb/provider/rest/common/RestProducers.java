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
package org.apache.servicecomb.provider.rest.common;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.provider.producer.ProducerMeta;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class RestProducers implements BeanPostProcessor {
  private List<ProducerMeta> producerMetaList = new ArrayList<>();

  @SuppressWarnings("unchecked")
  private Class<? extends Annotation> restControllerCls = (Class<? extends Annotation>) ReflectUtils
      .getClassByName("org.springframework.web.bind.annotation.RestController");

  private boolean scanRestController = restControllerCls != null &&
      DynamicPropertyFactory.getInstance().getBooleanProperty(RestConst.PROVIDER_SCAN_REST_CONTROLLER, true).get();

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
    if (beanCls == null) {
      return;
    }
    RestSchema restSchema = beanCls.getAnnotation(RestSchema.class);
    if (restSchema != null) {
      ProducerMeta producerMeta = new ProducerMeta(restSchema.schemaId(), bean);
      producerMeta.setSchemaInterface(restSchema.schemaInterface());
      producerMetaList.add(producerMeta);
      return;
    }

    if (scanRestController && beanCls.getAnnotation(restControllerCls) != null) {
      ProducerMeta producerMeta = new ProducerMeta(beanCls.getName(), bean);
      producerMetaList.add(producerMeta);
    }
  }
}
