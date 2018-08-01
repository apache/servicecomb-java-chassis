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

package org.apache.servicecomb.swagger.invocation.arguments.producer;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.junit.Assert;
import org.junit.Test;

public class ProducerSpringMVCQueryObjectMapperTest {

  @Test
  public void mapArgument() {
    final HashMap<String, Integer> swaggerParamIndexMap = new HashMap<>();
    swaggerParamIndexMap.put("name", 0);
    swaggerParamIndexMap.put("age", 1);
    ArgumentMapper argumentMapper = new ProducerSpringMVCQueryObjectMapper(swaggerParamIndexMap, 0, TestParam.class);
    SwaggerInvocation swaggerInvocation = new SwaggerInvocation();
    swaggerInvocation.setSwaggerArguments(new Object[] {"nameTest", 22});

    final Object[] producerArguments = new Object[1];
    argumentMapper.mapArgument(swaggerInvocation, producerArguments);
    Assert.assertEquals(producerArguments[0], new TestParam().setName("nameTest").setAge(22));
  }

  @Test
  public void mapArgumentOnRecursiveParam() {
    final HashMap<String, Integer> swaggerParamIndexMap = new HashMap<>();
    swaggerParamIndexMap.put("num", 0);
    swaggerParamIndexMap.put("str", 1);
    swaggerParamIndexMap.put("date", 2);
    ArgumentMapper argumentMapper = new ProducerSpringMVCQueryObjectMapper(swaggerParamIndexMap, 1,
        RecursiveParam.class);
    SwaggerInvocation swaggerInvocation = new SwaggerInvocation();
    final Date testDate = new Date();
    swaggerInvocation.setSwaggerArguments(new Object[] {2, "str0_0", testDate});

    final Object[] producerArguments = new Object[2];
    argumentMapper.mapArgument(swaggerInvocation, producerArguments);
    Assert.assertNull(producerArguments[0]);
    Assert.assertEquals(producerArguments[1], new RecursiveParam().setNum(2).setStr("str0_0").setDate(testDate));
  }

  static class TestParam {
    private String name;

    private int age;

    public String getName() {
      return name;
    }

    public TestParam setName(String name) {
      this.name = name;
      return this;
    }

    public int getAge() {
      return age;
    }

    public TestParam setAge(int age) {
      this.age = age;
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestParam testParam = (TestParam) o;
      return age == testParam.age &&
          Objects.equals(name, testParam.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, age);
    }
  }

  static class RecursiveParam {

    private int num;

    private String str;

    private Date date;

    private RecursiveParam recursiveParam;

    public int getNum() {
      return num;
    }

    public RecursiveParam setNum(int num) {
      this.num = num;
      return this;
    }

    public String getStr() {
      return str;
    }

    public RecursiveParam setStr(String str) {
      this.str = str;
      return this;
    }

    public Date getDate() {
      return date;
    }

    public RecursiveParam setDate(Date date) {
      this.date = date;
      return this;
    }

    public RecursiveParam getRecursiveParam() {
      return recursiveParam;
    }

    public RecursiveParam setRecursiveParam(
        RecursiveParam recursiveParam) {
      this.recursiveParam = recursiveParam;
      return this;
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("RecursiveParam{");
      sb.append("num=").append(num);
      sb.append(", str='").append(str).append('\'');
      sb.append(", date=").append(date);
      sb.append(", recursiveParam=").append(recursiveParam);
      sb.append('}');
      return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      RecursiveParam that = (RecursiveParam) o;
      return num == that.num &&
          Objects.equals(str, that.str) &&
          Objects.equals(date, that.date) &&
          Objects.equals(recursiveParam, that.recursiveParam);
    }

    @Override
    public int hashCode() {
      return Objects.hash(num, str, date, recursiveParam);
    }
  }
}