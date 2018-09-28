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
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

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

    Assert.assertEquals(String.class, beanDescriptor.getPropertyDescriptors().get("value").getJavaType().getRawClass());
  }

  @Test
  public void getOrCreate() {
    Assert.assertSame(beanDescriptor, beanDescriptorManager.getOrCreateBeanDescriptor(Model.class));
    Assert.assertSame(Model.class, beanDescriptor.getJavaType().getRawClass());
  }

  @Test
  public void both() throws Throwable {
    PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get("both");
    propertyDescriptor.getSetter().set(model, 1);
    Assert.assertEquals(1, propertyDescriptor.getGetter().get(model));
    Assert.assertEquals(1, model.getBoth());
  }

  @Test
  public void onlyGet() throws Throwable {
    PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get("onlyGet");
    Assert.assertNull(propertyDescriptor.getSetter());

    model.onlyGet(1);
    Assert.assertEquals(1, propertyDescriptor.getGetter().get(model));
    Assert.assertEquals(1, model.getOnlyGet());
  }

  @Test
  public void onlySet() throws Throwable {
    PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get("onlySet");
    Assert.assertNull(propertyDescriptor.getGetter());

    propertyDescriptor.getSetter().set(model, 1);
    Assert.assertEquals(1, model.onlySet());
  }

  @Test
  public void direct() throws Throwable {
    PropertyDescriptor propertyDescriptor = beanDescriptor.getPropertyDescriptors().get("direct");
    propertyDescriptor.getSetter().set(model, 1);
    Assert.assertEquals(1, propertyDescriptor.getGetter().get(model));
    Assert.assertEquals(1, model.direct);
  }
}
