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

import javax.inject.Inject;

import org.apache.servicecomb.core.provider.producer.ProducerMeta;
import org.springframework.beans.factory.InitializingBean;

public class PojoProducerMeta extends ProducerMeta implements InitializingBean {
  @Inject
  protected PojoProducers pojoProducers;

  private String implementation;

  private String schemaInterfaceName;

  public String getImplementation() {
    return implementation;
  }

  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }

  public String getSchemaInterfaceName() {
    return schemaInterfaceName;
  }

  public PojoProducerMeta setSchemaInterfaceName(String schemaInterfaceName) {
    this.schemaInterfaceName = schemaInterfaceName;
    return this;
  }

  @Override
  public void afterPropertiesSet() {
    pojoProducers.registerPojoProducer(this);
  }
}
