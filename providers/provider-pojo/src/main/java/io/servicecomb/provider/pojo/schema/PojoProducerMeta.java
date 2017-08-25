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

package io.servicecomb.provider.pojo.schema;

import javax.inject.Inject;

import org.springframework.beans.factory.InitializingBean;

import io.servicecomb.core.provider.producer.ProducerMeta;

public class PojoProducerMeta extends ProducerMeta implements InitializingBean {
  @Inject
  protected PojoProducers pojoProducers;

  private String implementation;

  public String getImplementation() {
    return implementation;
  }

  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    pojoProducers.registerPojoProducer(this);
  }
}
