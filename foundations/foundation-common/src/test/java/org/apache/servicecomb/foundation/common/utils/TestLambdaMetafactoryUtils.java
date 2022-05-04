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
package org.apache.servicecomb.foundation.common.utils;

import static org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils.createObjectGetter;
import static org.apache.servicecomb.foundation.common.utils.LambdaMetafactoryUtils.createObjectSetter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.IntGetter;
import org.apache.servicecomb.foundation.common.utils.bean.IntSetter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;

@EnabledOnJre(JRE.JAVA_8)
public class TestLambdaMetafactoryUtils {
  public static class Model {
    public int f1;

    private int f2;

    public int getF1() {
      return f1;
    }

    public void setF1(int f1) {
      this.f1 = f1;
    }

    public Model fluentSetF1(int f1) {
      this.f1 = f1;
      return this;
    }

    public List<Integer> echo(List<Integer> value) {
      return value;
    }
  }

  Model model = new Model();

  @SuppressWarnings("unchecked")
  @Test
  public void createLambda_withInstance() throws Throwable {
    Supplier<Object> getter = LambdaMetafactoryUtils
        .createLambda(model, Model.class.getMethod("getF1"), Supplier.class);
    Consumer<Object> setter = LambdaMetafactoryUtils
        .createLambda(model, Model.class.getMethod("setF1", int.class), Consumer.class);
    Function<Object, Object> echo = LambdaMetafactoryUtils
        .createLambda(model, Model.class.getMethod("echo", List.class), Function.class);

    setter.accept(1);
    int f1 = (int) getter.get();
    Assertions.assertEquals(1, f1);
    MatcherAssert.assertThat((List<Integer>) echo.apply(Arrays.asList(2)), Matchers.contains(2));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void createGetterSetterByMethod() throws Throwable {
    IntGetter<Model> getter = LambdaMetafactoryUtils.createGetter(Model.class.getMethod("getF1"));
    IntSetter<Model> setter = LambdaMetafactoryUtils.createSetter(Model.class.getMethod("setF1", int.class));
    IntSetter<Model> fluentSetter = LambdaMetafactoryUtils
        .createSetter(Model.class.getMethod("fluentSetF1", int.class));
    BiFunction<Object, Object, Object> echo = LambdaMetafactoryUtils
        .createLambda(Model.class.getMethod("echo", List.class), BiFunction.class);

    setter.set(model, 1);
    int f1 = getter.get(model);
    Assertions.assertEquals(1, f1);
    MatcherAssert.assertThat((List<Integer>) echo.apply(model, Arrays.asList(2)), Matchers.contains(2));

    fluentSetter.set(model, 2);
    int ff1 = getter.get(model);
    Assertions.assertEquals(2, ff1);
  }

  @Test
  public void should_failed_when_createGetterSetterByField_and_field_is_not_public() throws Throwable {
    Field field = Model.class.getDeclaredField("f2");
    assertThat(catchThrowable(() -> LambdaMetafactoryUtils.createGetter(field)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Can not access field, a public field or accessor is required.Declaring class is org.apache.servicecomb.foundation.common.utils.TestLambdaMetafactoryUtils$Model, field is f2");
    assertThat(catchThrowable(() -> LambdaMetafactoryUtils.createSetter(field)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Can not access field, a public field or accessor is required.Declaring class is org.apache.servicecomb.foundation.common.utils.TestLambdaMetafactoryUtils$Model, field is f2");
  }

  public static class Base<T> {
    private T base;

    public T getBase() {
      return base;
    }

    public Base<T> setBase(T base) {
      this.base = base;
      return this;
    }
  }

  public static class Child extends Base<Integer> {
    private int child;

    public int getChild() {
      return child;
    }

    public Child setChild(int child) {
      this.child = child;
      return this;
    }
  }

  @Test
  public void should_support_primitive_type() {
    Child child = new Child();

    ObjectMapper mapper = JsonUtils.OBJ_MAPPER;
    BeanDescription beanDescription = mapper.getSerializationConfig().introspect(mapper.constructType(Child.class));
    List<BeanPropertyDefinition> properties = beanDescription.findProperties();
    assertThat(properties).hasSize(2);

    for (int idx = 0; idx < properties.size(); idx++) {
      BeanPropertyDefinition property = properties.get(idx);

      Setter<Object, Object> setter = createObjectSetter(property.getSetter().getAnnotated());
      setter.set(child, idx);

      Getter<Object, Object> getter = createObjectGetter(property.getGetter().getAnnotated());
      Object value = getter.get(child);

      assertThat(value).isEqualTo(idx);
    }
  }
}
