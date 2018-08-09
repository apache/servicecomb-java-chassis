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

package org.apache.servicecomb.common.rest.codec.produce;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.vertx.core.buffer.Buffer;

public class TestProduceTextPlainProcessor {
  ProduceProcessor pp = ProduceProcessorManager.PLAIN_PROCESSOR;

  JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);

  @Test
  public void testEncodeResponseNull() throws Exception {
    Buffer buffer = pp.encodeResponse(null);
    Assert.assertNull(buffer);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    pp.encodeResponse(os, null);
    Assert.assertEquals(0, os.size());
  }

  @Test
  public void testdecodeResponseNull() throws Exception {
    JavaType resultType = TypeFactory.unknownType();
    Object result = pp.decodeResponse(Buffer.buffer(), resultType);
    Assert.assertNull(result);

    ByteArrayInputStream is = new ByteArrayInputStream(new byte[] {});
    result = pp.decodeResponse(is, resultType);
    Assert.assertEquals(result, "");
  }

  @Test
  public void testBufferNormal() throws Exception {
    String value = "abc";
    Buffer buffer = pp.encodeResponse(value);
    Assert.assertEquals(value, buffer.toString(StandardCharsets.UTF_8));

    Object result = pp.decodeResponse(buffer, stringType);
    Assert.assertEquals(value, result);
  }

  @Test
  public void testStreamNormal() throws Exception {
    String value = "abc";
    ByteArrayOutputStream os = new ByteArrayOutputStream();

    pp.encodeResponse(os, value);
    Assert.assertEquals(value, os.toString(StandardCharsets.UTF_8.name()));

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    Object result = pp.decodeResponse(is, stringType);
    Assert.assertEquals(value, result);

    os.close();
    is.close();
  }
}
