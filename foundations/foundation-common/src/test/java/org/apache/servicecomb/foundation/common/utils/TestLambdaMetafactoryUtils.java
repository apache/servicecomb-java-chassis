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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.servicecomb.foundation.common.utils.bean.IntGetter;
import org.apache.servicecomb.foundation.common.utils.bean.IntSetter;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

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
    Assert.assertEquals(1, f1);
    Assert.assertThat((List<Integer>) echo.apply(Arrays.asList(2)), Matchers.contains(2));
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
    Assert.assertEquals(1, f1);
    Assert.assertThat((List<Integer>) echo.apply(model, Arrays.asList(2)), Matchers.contains(2));

    fluentSetter.set(model, 2);
    int ff1 = getter.get(model);
    Assert.assertEquals(2, ff1);
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
}
