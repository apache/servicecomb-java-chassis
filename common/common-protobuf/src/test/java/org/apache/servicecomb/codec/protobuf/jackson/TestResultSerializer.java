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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufGenerator;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufField;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufMessage;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;

import mockit.Mock;
import mockit.MockUp;

public class TestResultSerializer {

  private ResultSerializer resultSerializer = null;

  OutputStream outputStream = null;

  @Before
  public void setUp() throws Exception {
    resultSerializer = new ResultSerializer();
    outputStream = new BufferOutputStream();
  }

  @After
  public void tearDown() throws Exception {
    resultSerializer = null;
    outputStream = null;
  }

  @Test
  public void testSerialize() {
    boolean status = true;
    Assert.assertNotNull(resultSerializer);
    String[] stringArray = new String[1];
    stringArray[0] = "abc";

    ProtobufGenerator obj = null;
    try {
      obj = new ProtobufGenerator(Mockito.mock(IOContext.class), 2, Mockito.mock(ObjectCodec.class),
          outputStream);
    } catch (IOException exce) {
    }

    Assert.assertNotNull(obj);

    new MockUp<ProtobufGenerator>() {

      @Mock
      public void writeStartObject() throws IOException {

      }

      ProtobufSchema protobufSchema = new ProtobufSchema(null, null);

      @Mock
      public ProtobufSchema getSchema() {
        return protobufSchema;
      }
    };
    ProtobufMessage protobufMessage = new ProtobufMessage(null, null);
    new MockUp<ProtobufSchema>() {
      @Mock
      public ProtobufMessage getRootType() {
        return protobufMessage;
      }
    };

    new MockUp<ProtobufMessage>() {
      @Mock
      public ProtobufField firstField() {
        return Mockito.mock(ProtobufField.class);
      }
    };

    new MockUp<JsonGenerator>() {
      @Mock
      public void writeObjectField(String fieldName, Object pojo) throws IOException {

      }
    };

    new MockUp<ProtobufGenerator>() {

      @Mock
      public void writeEndObject() throws IOException {

      }
    };

    try {
      resultSerializer.serialize(stringArray,
          obj,
          Mockito.mock(SerializerProvider.class));
    } catch (IOException e) {
      status = false;
    }

    Assert.assertTrue(status);
  }
}
