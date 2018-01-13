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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufOutput;
import io.protostuff.Schema;

public class TestArgsNotWrapSchema {

  private ArgsNotWrapSchema argsNotWrapSchema = null;

  @Before
  public void setUp() throws Exception {
    argsNotWrapSchema = new ArgsNotWrapSchema(Mockito.mock(Schema.class));
  }

  @After
  public void tearDown() throws Exception {
    argsNotWrapSchema = null;
  }

  @Test
  public void testReadFromEmpty() {
    Assert.assertNotNull(argsNotWrapSchema);
    Object object = argsNotWrapSchema.readFromEmpty();
    Assert.assertNotNull(object);
    // object is created but no values inside to assert
  }

  @Test
  public void testReadObject() {
    boolean status = true;
    Input input = null;
    try {
      Object object = argsNotWrapSchema.readObject(input);
      Assert.assertNotNull(object);
      // object is created but no values inside to assert
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }

  @Test
  public void testWriteObject() {
    boolean status = true;
    LinkedBuffer linkedBuffer = LinkedBuffer.allocate();
    ProtobufOutput output = new ProtobufOutput(linkedBuffer);
    String[] stringArray = new String[1];
    try {
      argsNotWrapSchema.writeObject(output, stringArray);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }

  @Test
  public void testWriteObjectToSchema() {
    boolean status = true;
    LinkedBuffer linkedBuffer = LinkedBuffer.allocate();
    ProtobufOutput output = new ProtobufOutput(linkedBuffer);
    String[] stringArray = new String[1];
    stringArray[0] = "abc";
    try {
      argsNotWrapSchema.writeObject(output, stringArray);
    } catch (Exception e) {
      status = false;
    }
    Assert.assertTrue(status);
  }
}
