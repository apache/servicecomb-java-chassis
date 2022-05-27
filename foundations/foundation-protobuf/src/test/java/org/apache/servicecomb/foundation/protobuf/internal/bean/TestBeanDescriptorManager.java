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

import java.lang.reflect.Method;

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.IntGetter;
import org.apache.servicecomb.foundation.common.utils.bean.IntSetter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestBeanDescriptorManager {
  public static class Model {
    private int both;

    private int onlyGet;

    private int onlySet;

    public int direct;

    public int getBoth() {
      return both;
    }

    public void setBoth(int both) {
      this.both = both;
    }

    public int getOnlyGet() {
      return onlyGet;
    }

    public void onlyGet(int value) {
      this.onlyGet = value;
    }

    public void setOnlySet(int onlySet) {
      this.onlySet = onlySet;
    }

    public int onlySet() {
      return onlySet;
    }
  }

  public static class CustomGeneric<T> {
    private T value;

    public T getValue() {
      return value;
    }

    public void setValue(T value) {
      this.value = value;
    }
  }

  public static class GenericSchema {
    public CustomGeneric<Model> genericMethod(CustomGeneric<String> input) {
      return null;
    }
  }

  static ObjectMapper mapper = new ObjectMapper();

  static BeanDescriptorManager beanDescriptorManager = new BeanDescriptorManager(mapper.getSerializationConfig());

  static BeanDescriptor beanDescriptor = beanDescriptorManager.getOrCreateBeanDescriptor(Model.class);

  Model model = new Model();

  @Test
  public void generic() {
    Method method = ReflectUtils.findMethod(GenericSchema.class, "genericMethod");
    BeanDescriptor beanDescriptor = beanDescriptorManager
        .getOrCreateBeanDescriptor(method.getGenericParameterTypes()[0]);

    Assertions.assertEquals(String.class, beanDescriptor.getPropertyDescriptors().get("value").getJavaType().getRawClass());
  }

  @Test
  public void getOrCreate() {
    Assertions.assertSame(beanDescriptor, beanDescriptorManager.getOrCreateBeanDescriptor(Model.class));
    Assertions.assertSame(Model.class, beanDescriptor.getJavaType().getRawClass());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void both() {
    PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get("both");
    ((IntSetter<Model>) propertyDescriptor.getSetter()).set(model, 1);
    Assertions.assertEquals(1, ((IntGetter<Model>) propertyDescriptor.getGetter()).get(model));
    Assertions.assertEquals(1, model.getBoth());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void onlyGet() {
    PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get("onlyGet");
    Assertions.assertNull(propertyDescriptor.getSetter());

    model.onlyGet(1);
    Assertions.assertEquals(1, ((IntGetter<Model>) propertyDescriptor.getGetter()).get(model));
    Assertions.assertEquals(1, model.getOnlyGet());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void onlySet() {
    PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get("onlySet");
    Assertions.assertNull(propertyDescriptor.getGetter());

    ((IntSetter<Model>) propertyDescriptor.getSetter()).set(model, 1);
    Assertions.assertEquals(1, model.onlySet());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void direct() {
    PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get("direct");
    ((Setter<Model, Integer>) propertyDescriptor.getSetter()).set(model, 1);
    Assertions.assertEquals(1, (int) ((Getter<Model, Integer>) propertyDescriptor.getGetter()).get(model));
    Assertions.assertEquals(1, model.direct);
  }
}
