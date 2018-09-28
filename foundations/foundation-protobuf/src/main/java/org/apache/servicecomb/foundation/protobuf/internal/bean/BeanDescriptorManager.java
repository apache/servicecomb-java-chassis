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

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class BeanDescriptorManager {
  private SerializationConfig serializationConfig;

  private Map<Type, BeanDescriptor> beanDescriptors = new ConcurrentHashMapEx<>();

  public BeanDescriptorManager(SerializationConfig serializationConfig) {
    this.serializationConfig = serializationConfig;
  }

  public BeanDescriptor getOrCreateBeanDescriptor(Type type) {
    return beanDescriptors.computeIfAbsent(type, this::createBeanDescriptor);
  }

  protected BeanDescriptor createBeanDescriptor(Type type) {
    return createBeanDescriptor(TypeFactory.defaultInstance().constructType(type));
  }

  protected BeanDescriptor createBeanDescriptor(JavaType javaType) {
    BeanDescriptor beanDescriptor = new BeanDescriptor();
    beanDescriptor.init(serializationConfig, javaType);
    return beanDescriptor;
  }
}
