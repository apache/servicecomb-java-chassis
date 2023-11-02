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
package org.apache.servicecomb.core.filter;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public abstract class AbstractFilter implements Filter, EnvironmentAware {
  private static final String ORDER_KEY = "servicecomb.filter.%s.%s.%s.order";

  private static final String ENABLE_KEY = "servicecomb.filter.%s.%s.%s.enabled";

  protected Environment environment;

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public int getOrder(String application, String serviceName) {
    Integer custom = environment.getProperty(String.format(ORDER_KEY, getName(), application, serviceName),
        Integer.class);
    if (custom != null) {
      return custom;
    }
    return getOrder();
  }

  @Override
  public boolean enabledForMicroservice(String application, String serviceName) {
    Boolean custom = environment.getProperty(String.format(ENABLE_KEY, getName(), application, serviceName),
        Boolean.class);
    if (custom != null) {
      return custom;
    }
    return true;
  }
}
