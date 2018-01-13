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

import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.beans.BeansException;
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
    ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
      public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        processConsumerField(bean, field);
      }
    });

    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    return bean;
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

  private void handleReferenceField(Object obj, Field field,
      RpcReference reference) {
    String microserviceName = reference.microserviceName();
    microserviceName = resolver.resolveStringValue(microserviceName);

    PojoReferenceMeta pojoReference = new PojoReferenceMeta();
    pojoReference.setMicroserviceName(microserviceName);
    pojoReference.setSchemaId(reference.schemaId());
    pojoReference.setConsumerIntf(field.getType());

    pojoReference.afterPropertiesSet();

    ReflectionUtils.makeAccessible(field);
    ReflectionUtils.setField(field, obj, pojoReference.getProxy());
  }
}
