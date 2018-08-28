package org.apache.servicecomb.swagger.invocation.validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class TestDefaultParameterNameProviderEx {
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

  DefaultParameterNameProviderEx parameterNameProviderEx = new DefaultParameterNameProviderEx();

  @Test
  public void testMethod() throws NoSuchMethodException {
    Method method = validatorForTest.getMethod("add", int.class, int.class);
    Assert.assertThat(parameterNameProviderEx.getParameterNames(method), Matchers.contains("a", "b"));
    method = validatorForTest.getMethod("sayHi", String.class);
    Assert.assertThat(parameterNameProviderEx.getParameterNames(method), Matchers.contains("hi"));
    method = validatorForTest.getMethod("sayHello", ValidatorForTest.Student.class);
    Assert.assertThat(parameterNameProviderEx.getParameterNames(method), Matchers.contains("student"));
    method = validatorForTest.getMethod("setTest", String.class);
    Assert.assertThat(parameterNameProviderEx.getParameterNames(method), Matchers.contains("grade"));
    method = validatorForTest.getMethod("getNumber");
    Assert.assertTrue(parameterNameProviderEx.getParameterNames(method).isEmpty());
    method = validatorForTest.getMethod("setNumber", int.class);
    Assert.assertThat(parameterNameProviderEx.getParameterNames(method), Matchers.contains("number"));
  }

  @Test
  public void testConstructor() throws NoSuchMethodException {
    Constructor<ValidatorForTest> constructor = validatorForTest.getConstructor(String.class, int.class);
    Assert.assertThat(parameterNameProviderEx.getParameterNames(constructor), Matchers.contains("grade", "number"));
    constructor = validatorForTest.getConstructor();
    Assert.assertTrue(parameterNameProviderEx.getParameterNames(constructor).isEmpty());

  }
}
