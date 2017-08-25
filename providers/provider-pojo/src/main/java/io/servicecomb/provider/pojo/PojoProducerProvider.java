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

package io.servicecomb.provider.pojo;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import io.servicecomb.core.definition.schema.ProducerSchemaFactory;
import io.servicecomb.core.provider.producer.AbstractProducerProvider;
import io.servicecomb.foundation.common.RegisterManager;
import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.provider.pojo.instance.PojoInstanceFactory;
import io.servicecomb.provider.pojo.instance.SpringInstanceFactory;
import io.servicecomb.provider.pojo.schema.PojoProducerMeta;
import io.servicecomb.provider.pojo.schema.PojoProducers;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.Microservice;

@Component
public class PojoProducerProvider extends AbstractProducerProvider {
  private RegisterManager<String, InstanceFactory> instanceFactoryMgr =
      new RegisterManager<>("pojo instance factory manager");

  @Inject
  private ProducerSchemaFactory producerSchemaFactory;

  @Inject
  private PojoProducers pojoProducers;

  public void regsiterInstanceFactory(InstanceFactory instanceFactory) {
    instanceFactoryMgr.register(instanceFactory.getImplName(), instanceFactory);
  }

  public PojoProducerProvider() {
    regsiterInstanceFactory(new PojoInstanceFactory());
    regsiterInstanceFactory(new SpringInstanceFactory());
  }

  @Override
  public void init() throws Exception {
    for (PojoProducerMeta pojoProducerMeta : pojoProducers.getProcucers()) {
      initPojoProducerMeta(pojoProducerMeta);

      Microservice microservice = RegistryUtils.getMicroservice();
      try {
        producerSchemaFactory.getOrCreateProducerSchema(
            microservice.getServiceName(),
            pojoProducerMeta.getSchemaId(),
            pojoProducerMeta.getInstanceClass(),
            pojoProducerMeta.getInstance());
      } catch (Throwable e) {
        throw new IllegalArgumentException(
            "create producer schema failed, class=" + pojoProducerMeta.getInstanceClass().getName(), e);
      }
    }
  }

  @Override
  public String getName() {
    return PojoConst.POJO;
  }

  private void initPojoProducerMeta(PojoProducerMeta pojoProducerMeta) {
    if (pojoProducerMeta.getInstance() != null) {
      return;
    }

    String[] nameAndValue = parseImplementation(pojoProducerMeta.getImplementation());

    InstanceFactory factory = instanceFactoryMgr.ensureFindValue(nameAndValue[0]);
    Object instance = factory.create(nameAndValue[1]);
    Class<?> instanceClass = BeanUtils.getImplClassFromBean(instance);

    pojoProducerMeta.setInstance(instance);
    pojoProducerMeta.setInstanceClass(instanceClass);
  }

  private String[] parseImplementation(String implementation) {
    String implName = PojoConst.POJO;
    String implValue = implementation;
    int idx = implementation.indexOf(':');
    if (idx != -1) {
      implName = implementation.substring(0, idx);
      implValue = implementation.substring(idx + 1);
    }

    return new String[] {implName, implValue};
  }
}
