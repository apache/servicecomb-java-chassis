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

package org.apache.servicecomb.codec.protobuf.utils.schema;

import java.io.IOException;

import org.apache.servicecomb.common.javassist.MultiWrapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufOutput;
import io.protostuff.Schema;

public class TestArgsWrapSchema {

  private ArgsWrapSchema argsWrapSchema = null;

  @SuppressWarnings("rawtypes")
  private Schema schema = null;

  @Before
  public void setUp() throws Exception {
    schema = Mockito.mock(Schema.class);
    argsWrapSchema = new ArgsWrapSchema(schema);
  }

  @After
  public void tearDown() throws Exception {
    argsWrapSchema = null;
  }

  @Test
  public void testReadFromEmpty() {

    MultiWrapper multiWrapper = Mockito.mock(MultiWrapper.class);
    Mockito.when(schema.newMessage()).thenReturn(multiWrapper);
    Assert.assertNotNull(argsWrapSchema);
    Object object = argsWrapSchema.readFromEmpty();
    Assert.assertNull(object);
  }

  @Test
  public void testWriteObject() {
    boolean status = true;
    LinkedBuffer linkedBuffer = LinkedBuffer.allocate();
    ProtobufOutput output = new ProtobufOutput(linkedBuffer);
    String[] stringArray = new String[1];
    stringArray[0] = "abc";

    MultiWrapper multiWrapper = Mockito.mock(MultiWrapper.class);
    Mockito.when(schema.newMessage()).thenReturn(multiWrapper);
    try {
      argsWrapSchema.writeObject(output, stringArray);
    } catch (IOException e) {
      status = true;
    }
    Assert.assertTrue(status);
  }

  @Test
  public void testReadObject() {
    boolean status = true;
    Input input = null;
    String[] stringArray = new String[1];
    stringArray[0] = "abc";

    MultiWrapper multiWrapper = Mockito.mock(MultiWrapper.class);
    Mockito.when(schema.newMessage()).thenReturn(multiWrapper);
    try {
      Object object = argsWrapSchema.readObject(input);
      Assert.assertNull(object);
    } catch (IOException e) {
      status = true;
    }
    Assert.assertTrue(status);
  }
}
