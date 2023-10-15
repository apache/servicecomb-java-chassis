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
package org.apache.servicecomb.config.priority;

import org.springframework.core.env.Environment;

public class DynamicProperty {
  private final Environment environment;

  private final String propName;

  public DynamicProperty(Environment environment, String propName) {
    this.environment = environment;
    this.propName = propName;
  }

  public Integer getInteger() {
    return environment.getProperty(propName, Integer.class);
  }

  public Long getLong() {
    return environment.getProperty(propName, Long.class);
  }

  public String getString() {
    return environment.getProperty(propName);
  }

  public Boolean getBoolean() {
    return environment.getProperty(propName, Boolean.class);
  }

  public Double getDouble() {
    return environment.getProperty(propName, Double.class);
  }

  public Float getFloat() {
    return environment.getProperty(propName, Float.class);
  }

  public String getName() {
    return propName;
  }
}
