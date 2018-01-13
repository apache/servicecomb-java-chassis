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

package org.apache.servicecomb.codec.protobuf.codec;

import java.lang.reflect.Type;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.dataformat.protobuf.protoparser.protoparser.FieldElement;
import com.fasterxml.jackson.dataformat.protobuf.schema.FieldType;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufField;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufMessage;
import com.fasterxml.jackson.dataformat.protobuf.schema.ProtobufSchema;

public class TestStandardResultCodec {

  private StandardResultCodec standardResultCodec = null;

  private ProtobufSchema schema = null;

  public final static String FORMAT_NAME_PROTOBUF = "protobuf";

  @Before
  public void setUp() throws Exception {
    standardResultCodec = new StandardResultCodec();
    schema = Mockito.mock(ProtobufSchema.class);
  }

  @After
  public void tearDown() throws Exception {
    standardResultCodec = null;
    schema = null;
  }

  @Test
  public void testInit() {
    Assert.assertNotNull(standardResultCodec);

    ProtobufField[] protobufFieldArray = new ProtobufField[5];
    FieldElement rawType = null;
    FieldType type = FieldType.STRING;
    ProtobufField p = new ProtobufField(rawType, type);
    protobufFieldArray[0] = p;
    Type[] types = new Type[1];
    types[0] = Integer.TYPE;

    Mockito.when(schema.getSchemaType()).thenReturn(FORMAT_NAME_PROTOBUF);
    Mockito.when(schema.getRootType()).thenReturn(Mockito.mock(ProtobufMessage.class));
    Mockito.when(schema.getRootType().getFieldCount()).thenReturn(1);
    Mockito.when(schema.getRootType().fields()).thenReturn(Arrays.asList(protobufFieldArray));
    Assert.assertNull(standardResultCodec.reader);
    Assert.assertNull(standardResultCodec.writer);
    standardResultCodec.init(schema, types);
    Assert.assertNotNull(standardResultCodec.reader);
    Assert.assertNotNull(standardResultCodec.writer);
  }
}
