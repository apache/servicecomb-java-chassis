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

import javax.servlet.http.Part;

import org.apache.servicecomb.foundation.common.part.AbstractPart;
import org.apache.servicecomb.swagger.invocation.SwaggerInvocation;
import org.apache.servicecomb.swagger.invocation.arguments.ArgumentMapper;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerSpringMVCQueryObjectMapperTest.RecursiveParam;
import org.apache.servicecomb.swagger.invocation.arguments.producer.ProducerSpringMVCQueryObjectMapperTest.TestParam;
import org.junit.Assert;
import org.junit.Test;

public class ProducerBeanParamMapperTest {

  @Test
  public void mapArgument() {
    final HashMap<String, Integer> producerNameToSwaggerIndexMap = new HashMap<>();
    producerNameToSwaggerIndexMap.put("name", 2);
    producerNameToSwaggerIndexMap.put("age", 0);
    ArgumentMapper argumentMapper = new ProducerBeanParamMapper(producerNameToSwaggerIndexMap, 0, TestParam.class);
    SwaggerInvocation swaggerInvocation = new SwaggerInvocation();
    swaggerInvocation.setSwaggerArguments(new Object[] {22, "abc", "nameTest"});

    final Object[] producerArguments = new Object[1];
    argumentMapper.mapArgument(swaggerInvocation, producerArguments);
    Assert.assertEquals(producerArguments[0], new TestParam().setName("nameTest").setAge(22));
  }

  @Test
  public void mapArgumentOnRecursiveParam() {
    final HashMap<String, Integer> producerNameToSwaggerIndexMap = new HashMap<>();
    producerNameToSwaggerIndexMap.put("num", 0);
    producerNameToSwaggerIndexMap.put("str", 1);
    producerNameToSwaggerIndexMap.put("date", 2);
    ArgumentMapper argumentMapper = new ProducerBeanParamMapper(producerNameToSwaggerIndexMap, 1,
        RecursiveParam.class);
    SwaggerInvocation swaggerInvocation = new SwaggerInvocation();
    final Date testDate = new Date();
    swaggerInvocation.setSwaggerArguments(new Object[] {2, "str0_0", testDate});

    final Object[] producerArguments = new Object[2];
    argumentMapper.mapArgument(swaggerInvocation, producerArguments);
    Assert.assertNull(producerArguments[0]);
    Assert.assertEquals(producerArguments[1], new RecursiveParam().setNum(2).setStr("str0_0").setDate(testDate));
  }

  @Test
  public void mapArgumentWithPart() {
    final HashMap<String, Integer> producerNameToSwaggerIndexMap = new HashMap<>();
    producerNameToSwaggerIndexMap.put("up", 0);
    producerNameToSwaggerIndexMap.put("str", 2);
    producerNameToSwaggerIndexMap.put("longValue", 3);
    ArgumentMapper argumentMapper = new ProducerBeanParamMapper(producerNameToSwaggerIndexMap, 0,
        TestParamWithPart.class);
    SwaggerInvocation swaggerInvocation = new SwaggerInvocation();
    final AbstractPart uploadedFile = new AbstractPart();
    swaggerInvocation.setSwaggerArguments(new Object[] {uploadedFile, 123L, "testString", 12L});

    final Object[] producerArguments = new Object[2];
    argumentMapper.mapArgument(swaggerInvocation, producerArguments);
    Assert.assertEquals(producerArguments[0], new TestParamWithPart("testString", 12L, uploadedFile));
    Assert.assertSame(((TestParamWithPart) producerArguments[0]).getUp(), uploadedFile);
    Assert.assertNull(producerArguments[1]);
  }

  static class TestParamWithPart {
    private String str;

    private long longValue;

    private Part uploaded;

    public TestParamWithPart() {
    }

    public TestParamWithPart(String str, long longValue, Part uploaded) {
      this.str = str;
      this.longValue = longValue;
      this.uploaded = uploaded;
    }

    public String getStr() {
      return str;
    }

    public void setStr(String str) {
      this.str = str;
    }

    public long getLongValue() {
      return longValue;
    }

    public void setLongValue(long longValue) {
      this.longValue = longValue;
    }

    public Part getUp() {
      return uploaded;
    }

    public void setUp(Part uploaded) {
      this.uploaded = uploaded;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TestParamWithPart that = (TestParamWithPart) o;
      return longValue == that.longValue &&
          Objects.equals(str, that.str) &&
          Objects.equals(uploaded, that.uploaded);
    }

    @Override
    public int hashCode() {
      return Objects.hash(str, longValue, uploaded);
    }
  }
}
