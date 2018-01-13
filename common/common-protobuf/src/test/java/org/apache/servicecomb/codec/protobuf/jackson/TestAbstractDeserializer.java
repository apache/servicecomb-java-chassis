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

package org.apache.servicecomb.codec.protobuf.jackson;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.codec.protobuf.codec.AbstractFieldCodec.ReaderHelpData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class TestAbstractDeserializer extends AbstractDeserializer {

  private AbstractDeserializer abstractDeserializer = null;

  private JsonParser jsonParser = Mockito.mock(JsonParser.class);

  static ReaderHelpData readerHelpData = Mockito.mock(ReaderHelpData.class);

  static Map<String, ReaderHelpData> readerHelpDataMap = new HashMap<>();

  public static void setReaderHelpDataMap(Map<String, ReaderHelpData> readerHelpDataMap) {
    TestAbstractDeserializer.readerHelpDataMap = readerHelpDataMap;
    readerHelpDataMap.put("abc", readerHelpData);
    readerHelpDataMap.put("null", readerHelpData);
  }

  static {
    TestAbstractDeserializer.setReaderHelpDataMap(readerHelpDataMap);
  }

  public TestAbstractDeserializer() {
    super(readerHelpDataMap);
  }

  @Before
  public void setUp() throws Exception {
    abstractDeserializer = new TestAbstractDeserializer();
  }

  @After
  public void tearDown() throws Exception {
    abstractDeserializer = null;
    jsonParser = null;
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeserialize() {
    boolean status = false;
    try {
      DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
      @SuppressWarnings("rawtypes")
      JsonDeserializer JsonDeserializer = Mockito.mock(JsonDeserializer.class);
      Object object = null;
      Mockito.when(jsonParser.nextFieldName()).thenReturn("abc", (String) null);
      Mockito.when(readerHelpData.getDeser()).thenReturn(JsonDeserializer);
      Mockito.when(JsonDeserializer.deserialize(jsonParser, ctxt)).thenReturn(object);
      Object deserializeObject = abstractDeserializer.deserialize(jsonParser, ctxt);
      Assert.assertNotNull(deserializeObject);
    } catch (Exception e) {
      status = true;
    }
    Assert.assertFalse(status);
  }

  @Override
  protected Object createResult() {
    return null;
  }

  @Override
  protected Object updateResult(Object result, Object value, ReaderHelpData helpData) {
    /* Do not worry, overridden method*/
    try {
      Mockito.when(jsonParser.nextToken()).thenReturn(JsonToken.VALUE_NULL);
    } catch (Exception e) {
    }
    return new Object();
  }
}
