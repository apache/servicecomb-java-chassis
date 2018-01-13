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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import mockit.Mock;
import mockit.MockUp;

public class TestStandardObjectWriter {

  private StandardObjectWriter StandardObjectWriter = null;

  private OutputStream outputStream = null;

  @Before
  public void setUp() throws Exception {
    StandardObjectWriter = new StandardObjectWriter(Mockito.mock(ObjectWriter.class));
    outputStream = new BufferOutputStream();
  }

  @After
  public void tearDown() throws Exception {
    StandardObjectWriter = null;
    outputStream = null;
  }

  @Test
  public void testWriteValueOutputStreamObject() {
    boolean status = true;
    String[] stringArray = new String[1];
    stringArray[0] = "abc";

    new MockUp<ObjectWriter>() {
      @Mock
      public void writeValue(OutputStream out,
          Object value) throws IOException, JsonGenerationException, JsonMappingException {

      }
    };
    try {
      StandardObjectWriter.writeValue(outputStream,
          stringArray);
    } catch (IOException e) {
      status = false;
    }

    Assert.assertTrue(status);
  }
}
