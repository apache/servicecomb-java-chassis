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

package org.apache.servicecomb.provider.pojo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.provider.producer.AbstractProducerProvider;
import org.apache.servicecomb.core.provider.producer.ProducerMeta;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.provider.pojo.instance.PojoInstanceFactory;
import org.apache.servicecomb.provider.pojo.instance.SpringInstanceFactory;
import org.apache.servicecomb.provider.pojo.schema.PojoProducerMeta;
import org.apache.servicecomb.provider.pojo.schema.PojoProducers;

public class PojoProducerProvider extends AbstractProducerProvider {
  private Map<String, InstanceFactory> instanceFactoryMgr = new HashMap<>();

  private void registerInstanceFactory(InstanceFactory instanceFactory) {
    instanceFactoryMgr.put(instanceFactory.getImplName(), instanceFactory);
  }

  public PojoProducerProvider() {
    registerInstanceFactory(new PojoInstanceFactory());
    registerInstanceFactory(new SpringInstanceFactory());
  }

  @Override
  public List<ProducerMeta> init() {
    // for some test cases, there is no spring context
    if (BeanUtils.getContext() == null) {
      return Collections.emptyList();
    }

    PojoProducers pojoProducers = BeanUtils.getContext().getBean(PojoProducers.class);
    for (ProducerMeta producerMeta : pojoProducers.getProducerMetas()) {
      PojoProducerMeta pojoProducerMeta = (PojoProducerMeta) producerMeta;
      initPojoProducerMeta(pojoProducerMeta);
    }

    return pojoProducers.getProducerMetas();
  }

  @Override
  public String getName() {
    return PojoConst.POJO;
  }

  private void initPojoProducerMeta(PojoProducerMeta pojoProducerMeta) {
    parseSchemaInterface(pojoProducerMeta);
    parseImplementation(pojoProducerMeta);
  }

  private void parseImplementation(PojoProducerMeta pojoProducerMeta) {
    if (pojoProducerMeta.getInstance() != null) {
      return;
    }

    String implementation = pojoProducerMeta.getImplementation();
    String implName = PojoConst.POJO;
    String implValue = pojoProducerMeta.getImplementation();
    int idx = implementation.indexOf(':');
    if (idx != -1) {
      implName = implementation.substring(0, idx);
      implValue = implementation.substring(idx + 1);
    }

    InstanceFactory factory = instanceFactoryMgr.get(implName);
    if (factory == null) {
      throw new IllegalStateException("failed to find instance factory, name=" + implName);
    }

    Object instance = factory.create(implValue);
    pojoProducerMeta.setInstance(instance);
  }

  private void parseSchemaInterface(PojoProducerMeta pojoProducerMeta) {
    if (pojoProducerMeta.getSchemaInterface() != null || StringUtils
        .isEmpty(pojoProducerMeta.getSchemaInterfaceName())) {
      return;
    }

    try {
      Class<?> si = Class.forName(pojoProducerMeta.getSchemaInterfaceName());
      pojoProducerMeta.setSchemaInterface(si);
    } catch (Exception e) {
      throw new Error("can not find schema interface " + pojoProducerMeta.getSchemaInterfaceName(), e);
    }
  }
}
