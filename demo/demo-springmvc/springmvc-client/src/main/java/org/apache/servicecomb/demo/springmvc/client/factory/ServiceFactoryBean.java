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

package org.apache.servicecomb.demo.springmvc.client.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ServiceFactoryBean")
public class ServiceFactoryBean implements FactoryBean<ServiceBean> {

  ServiceWithReference serviceWithReference;

  @Autowired
  public ServiceFactoryBean(ServiceWithReference serviceWithReference) {
    this.serviceWithReference = serviceWithReference;
  }

  @Override
  public ServiceBean getObject() throws Exception {
    return new ServiceBean(serviceWithReference.test("a"));
  }

  @Override
  public Class<?> getObjectType() {
    return ServiceBean.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
