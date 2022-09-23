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
package org.apache.servicecomb.provider.pojo.reference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

@Component
public class RpcReferenceProcessor implements BeanPostProcessor, EmbeddedValueResolverAware {
  private StringValueResolver resolver;

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    // 扫描所有field，处理扩展的field标注
    ReflectionUtils.doWithFields(bean.getClass(), field -> processConsumerField(bean, field));
    ReflectionUtils.doWithMethods(bean.getClass(), method -> processConsumerMethod(bean, beanName, method));
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  protected void processConsumerMethod(Object bean, String beanName, Method method) throws BeansException {
    RpcReference reference = method.getAnnotation(RpcReference.class);
    if (reference == null) {
      return;
    }
    handleReferenceMethod(bean, beanName, method, reference);
  }

  protected void processConsumerField(Object bean, Field field) {
    RpcReference reference = field.getAnnotation(RpcReference.class);
    if (reference == null) {
      return;
    }

    handleReferenceField(bean, field, reference);
  }

  @Override
  public void setEmbeddedValueResolver(StringValueResolver resolver) {
    this.resolver = resolver;
  }

  private void handleReferenceMethod(Object bean, String beanName, Method method, RpcReference reference)
      throws BeansException {
    try {
      PojoReferenceMeta pojoReference = createPojoReferenceMeta(reference, method.getParameterTypes()[0]);
      method.invoke(bean, pojoReference.getProxy());
    } catch (Exception e) {
      throw new BeanCreationException(beanName, "", e);
    }
  }

  private void handleReferenceField(Object obj, Field field,
      RpcReference reference) {
    PojoReferenceMeta pojoReference = createPojoReferenceMeta(reference, field.getType());
    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, obj, pojoReference.getProxy());
  }

  private PojoReferenceMeta createPojoReferenceMeta(RpcReference reference, Class<?> consumerInterface) {
    String microserviceName = reference.microserviceName();
    microserviceName = resolver.resolveStringValue(microserviceName);

    PojoReferenceMeta pojoReference = new PojoReferenceMeta();
    pojoReference.setMicroserviceName(microserviceName);
    pojoReference.setSchemaId(reference.schemaId());
    pojoReference.setConsumerIntf(consumerInterface);
    pojoReference.afterPropertiesSet();
    return pojoReference;
  }
}
