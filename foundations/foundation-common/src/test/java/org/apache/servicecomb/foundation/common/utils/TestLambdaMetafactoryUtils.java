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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.servicecomb.foundation.common.utils.bean.Getter;
import org.apache.servicecomb.foundation.common.utils.bean.Setter;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestLambdaMetafactoryUtils {
  public static class Model {
    private int f1;

    public int getF1() {
      return f1;
    }

    public void setF1(int f1) {
      this.f1 = f1;
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
    Getter getter = LambdaMetafactoryUtils.createGetter(Model.class.getMethod("getF1"));
    Setter setter = LambdaMetafactoryUtils.createSetter(Model.class.getMethod("setF1", int.class));
    BiFunction<Object, Object, Object> echo = LambdaMetafactoryUtils
        .createLambda(Model.class.getMethod("echo", List.class), BiFunction.class);

    setter.set(model, 1);
    int f1 = (int) getter.get(model);
    Assert.assertEquals(1, f1);
    Assert.assertThat((List<Integer>) echo.apply(model, Arrays.asList(2)), Matchers.contains(2));
  }

  @Test
  public void createGetterSetterByField() throws Throwable {
    Field f1 = Model.class.getDeclaredField("f1");
    Getter getter = LambdaMetafactoryUtils.createGetter(f1);
    Setter setter = LambdaMetafactoryUtils.createSetter(f1);

    setter.set(model, 1);
    Assert.assertEquals(1, (int) getter.get(model));
  }
}
