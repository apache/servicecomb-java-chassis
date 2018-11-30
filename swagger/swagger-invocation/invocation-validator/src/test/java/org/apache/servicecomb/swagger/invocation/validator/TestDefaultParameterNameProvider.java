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
package org.apache.servicecomb.swagger.invocation.validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestDefaultParameterNameProvider {
  static class ValidatorForTest {
    static class Student {
      private String name;

      int age;

      public String getName() {
        return name;
      }

      public void setName(String name) {
        this.name = name;
      }

      public int getAge() {
        return age;
      }

      public void setAge(int age) {
        this.age = age;
      }
    }

    private String grade;

    private int number;

    public ValidatorForTest() {
    }

    public ValidatorForTest(String grade, int number) {
      this.grade = grade;
      this.number = number;
    }

    public int add(int a, int b) {
      return a + b;
    }

    public String sayHi(String hi) {
      return hi + " sayhi";
    }

    public Student sayHello(Student student) {
      return student;
    }

    public String setTest(String grade) {
      this.grade = grade;
      return this.grade;
    }

    public int getNumber() {
      return number;
    }

    public void setNumber(int number) {
      this.number = number;
    }
  }

  Class<ValidatorForTest> validatorForTest = ValidatorForTest.class;

  DefaultParameterNameProvider parameterNameProvider = new DefaultParameterNameProvider();

  @Test
  public void testMethod() throws NoSuchMethodException {
    Method method = validatorForTest.getMethod("add", int.class, int.class);
    Assert.assertThat(parameterNameProvider.getParameterNames(method), Matchers.contains("a", "b"));
    method = validatorForTest.getMethod("sayHi", String.class);
    Assert.assertThat(parameterNameProvider.getParameterNames(method), Matchers.contains("hi"));
    method = validatorForTest.getMethod("sayHello", ValidatorForTest.Student.class);
    Assert.assertThat(parameterNameProvider.getParameterNames(method), Matchers.contains("student"));
    method = validatorForTest.getMethod("setTest", String.class);
    Assert.assertThat(parameterNameProvider.getParameterNames(method), Matchers.contains("grade"));
    method = validatorForTest.getMethod("getNumber");
    Assert.assertTrue(parameterNameProvider.getParameterNames(method).isEmpty());
    method = validatorForTest.getMethod("setNumber", int.class);
    Assert.assertThat(parameterNameProvider.getParameterNames(method), Matchers.contains("number"));
  }

  @Test
  public void testConstructor() throws NoSuchMethodException {
    Constructor<ValidatorForTest> constructor = validatorForTest.getConstructor(String.class, int.class);
    Assert.assertThat(parameterNameProvider.getParameterNames(constructor), Matchers.contains("grade", "number"));
    constructor = validatorForTest.getConstructor();
    Assert.assertTrue(parameterNameProvider.getParameterNames(constructor).isEmpty());

  }
}
