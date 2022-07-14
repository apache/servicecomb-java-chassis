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
package org.apache.servicecomb.common.rest.codec.fix;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import org.apache.servicecomb.foundation.common.utils.RestObjectMapper;
import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

public class TestDoSFix {
  static final ObjectMapper mapper = new RestObjectMapper();

  static final String invalidNum = Strings.repeat("9", 100_0000);

  static final String invalidStr = "\"" + invalidNum + "\"";

  static final String invalidArrNum = "[" + invalidNum + "]";

  static final String invalidArrStr = "[\"" + invalidNum + "\"]";

  public static class Model {
    public Color color;

    public char cValue;

    public Character cObjValue;

    public byte bValue;

    public Byte bObjValue;

    public short sValue;

    public Short sObjValue;

    public int iValue;

    public Integer iObjValue;

    public long lValue;

    public Long lObjValue;

    public float fValue;

    public Float fObjValue;

    public double dValue;

    public Double dObjValue;
  }

  void fastFail(Callable<?> callable, Class<?> eCls) {
    long start = System.currentTimeMillis();
    try {
      Object ret = callable.call();
      Assertions.fail("expect failed, but succes to be " + ret);
    } catch (AssertionError e) {
      throw e;
    } catch (Throwable e) {
      if (eCls != e.getClass()) {
        e.printStackTrace();
      }
      Assertions.assertEquals(eCls, e.getClass());
    }

    long time = System.currentTimeMillis() - start;
    Assertions.assertTrue(time < 1000, "did not fix DoS problem, time:" + time);
  }

  void fastFail(String input, Class<?> cls, Class<?> eCls) {
    fastFail(() -> mapper.readValue(input, cls), eCls);

    fastFail(() -> mapper.readValue(new ByteArrayInputStream(input.getBytes()), cls), eCls);
  }

  void batFastFail(Class<?> cls, Class<?> e1, Class<?> e2) {
    fastFail(invalidNum, cls, e1);
    fastFail(invalidStr, cls, e2);
    fastFail(invalidArrNum, cls, e1);
    fastFail(invalidArrStr, cls, e2);
  }

  void batFastFail(Class<?> cls) {
    batFastFail(cls, InputCoercionException.class, InvalidFormatException.class);
  }

  void batFastFail(String fieldName, Class<?> e1, Class<?> e2) {
    fastFail("{\"" + fieldName + "\":" + invalidNum + "}", Model.class, e1);
    fastFail("{\"" + fieldName + "\":\"" + invalidNum + "\"}", Model.class, e2);
    fastFail("{\"" + fieldName + "\":[" + invalidNum + "]}", Model.class, e1);
    fastFail("{\"" + fieldName + "\":[\"" + invalidNum + "\"]}", Model.class, e2);
  }

  void batFastFail(String fieldName) {
    batFastFail(fieldName, JsonMappingException.class, InvalidFormatException.class);
  }

  @Test
  public void testEnum() {
    batFastFail(Color.class);
    batFastFail("color");
  }

  @Test
  public void testChar() {
    batFastFail(char.class, InputCoercionException.class, InvalidFormatException.class);
    batFastFail(Character.class, InputCoercionException.class, InvalidFormatException.class);

    batFastFail("cValue", JsonMappingException.class, InvalidFormatException.class);
    batFastFail("cObjValue", JsonMappingException.class, InvalidFormatException.class);
  }

  @Test
  public void testByte() {
    batFastFail(byte.class);
    batFastFail(Byte.class);

    batFastFail("bValue");
    batFastFail("bObjValue");
  }

  @Test
  public void testShort() {
    batFastFail(short.class);
    batFastFail(Short.class);

    batFastFail("sValue");
    batFastFail("sObjValue");
  }

  @Test
  public void testInt() {
    batFastFail(int.class);
    batFastFail(Integer.class);

    batFastFail("iValue");
    batFastFail("iObjValue");
  }

  @Test
  public void testLong() {
    batFastFail(long.class);
    batFastFail(Long.class);

    batFastFail("lValue");
    batFastFail("lObjValue");
  }

  Object fastSucc(Callable<?> callable) {
    long start = System.currentTimeMillis();
    try {
      Object ret = callable.call();
      Assertions.assertTrue(System.currentTimeMillis() - start < 1000);
      return ret;
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }

  Object fastSucc(String input, Class<?> cls) {
    return fastSucc(() -> mapper.readValue(input, cls));
  }

  Object fastSucc(InputStream input, Class<?> cls) {
    return fastSucc(() -> {
      input.reset();
      return mapper.readValue(input, cls);
    });
  }

  void batFastSucc(Class<?> cls, Object expected) {
    Assertions.assertEquals(expected, fastSucc(invalidNum, cls));
    Assertions.assertEquals(expected, fastSucc(new ByteArrayInputStream(invalidNum.getBytes()), cls));

    Assertions.assertEquals(expected, fastSucc(invalidStr, cls));
    Assertions.assertEquals(expected, fastSucc(new ByteArrayInputStream(invalidStr.getBytes()), cls));

    Assertions.assertEquals(expected, fastSucc(invalidArrNum, cls));
    Assertions.assertEquals(expected, fastSucc(new ByteArrayInputStream(invalidArrNum.getBytes()), cls));

    Assertions.assertEquals(expected, fastSucc(invalidArrStr, cls));
    Assertions.assertEquals(expected, fastSucc(new ByteArrayInputStream(invalidArrStr.getBytes()), cls));
  }

  void checkField(Model model, String fieldName, Object expected) {
    try {
      Field field = Model.class.getField(fieldName);
      Object value = field.get(model);
      Assertions.assertEquals(expected, value);
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }

  void batFastSucc(String fieldName, Object expected) {
    checkField((Model) fastSucc("{\"" + fieldName + "\":" + invalidNum + "}", Model.class), fieldName, expected);
    checkField((Model) fastSucc("{\"" + fieldName + "\":\"" + invalidNum + "\"}", Model.class), fieldName, expected);
    checkField((Model) fastSucc("{\"" + fieldName + "\":[" + invalidNum + "]}", Model.class), fieldName, expected);
    checkField((Model) fastSucc("{\"" + fieldName + "\":[\"" + invalidNum + "\"]}", Model.class), fieldName, expected);
  }

  @Test
  public void testFloat() {
    batFastSucc(float.class, Float.POSITIVE_INFINITY);
    batFastSucc(Float.class, Float.POSITIVE_INFINITY);

    batFastSucc("fValue", Float.POSITIVE_INFINITY);
    batFastSucc("fObjValue", Float.POSITIVE_INFINITY);
  }

  @Test
  public void testDouble() {
    batFastSucc(double.class, Double.POSITIVE_INFINITY);
    batFastSucc(Double.class, Double.POSITIVE_INFINITY);

    batFastSucc("dValue", Double.POSITIVE_INFINITY);
    batFastSucc("dObjValue", Double.POSITIVE_INFINITY);
  }
}
