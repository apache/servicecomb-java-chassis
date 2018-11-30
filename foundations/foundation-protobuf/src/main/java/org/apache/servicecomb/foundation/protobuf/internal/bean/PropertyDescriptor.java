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
package org.apache.servicecomb.foundation.protobuf.internal.bean;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;

import com.fasterxml.jackson.databind.JavaType;

public class PropertyDescriptor {
  private String name;

  private JavaType javaType;

  private Getter getter;

  private Setter setter;

  // not available for primitive types
  private BeanFactory factory;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JavaType getJavaType() {
    return javaType;
  }

  public void setJavaType(JavaType javaType) {
    this.javaType = javaType;
  }

  public Getter getGetter() {
    return getter;
  }

  public void setGetter(Getter getter) {
    this.getter = getter;
  }

  public Setter getSetter() {
    return setter;
  }

  public void setSetter(Setter setter) {
    this.setter = setter;
  }

  public BeanFactory getFactory() {
    return factory;
  }

  public void setFactory(BeanFactory factory) {
    this.factory = factory;
  }
}
