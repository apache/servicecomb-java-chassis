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

package org.apache.servicecomb.foundation.vertx.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestBufferInputStream {

  private BufferInputStream instance;

  @Before
  public void setUp() throws Exception {
    ByteBuf buffer = Mockito.mock(ByteBuf.class);
    instance = new BufferInputStream(buffer);
  }

  @After
  public void tearDown() throws Exception {
    instance = null;
  }

  @Test
  public void testRead() {
    Assertions.assertEquals(0, instance.read());
  }

  @Test
  public void testReadDecorate() throws IOException {
    String text = "abcdefg123456789";
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out);
    gzipOutputStream.write(text.getBytes());
    gzipOutputStream.close();

    ByteBuf buffer = Unpooled.buffer();
    buffer.writeBytes(out.toByteArray());
    out.close();
    BufferInputStream bufferInputStream = new BufferInputStream(buffer);
    GZIPInputStream gzipInputStream = new GZIPInputStream(bufferInputStream);
    StringBuilder sb = new StringBuilder();
    byte[] bufferByte = new byte[256];
    int n;
    while ((n = gzipInputStream.read(bufferByte)) >= 0) {
      sb.append(new String(bufferByte, 0, n));
    }
    gzipInputStream.close();

    Assertions.assertEquals(text, sb.toString());
  }

  @Test
  public void testReadByteArray() {
    byte[] b = "csr".getBytes(StandardCharsets.UTF_8);
    Assertions.assertEquals(-1, instance.read(b));
  }

  @Test
  public void testReadByteArrayIntInt() {
    byte[] b = "csr".getBytes(StandardCharsets.UTF_8);
    Assertions.assertEquals(-1, instance.read(b, 1, 0));
  }

  @Test
  public void testSkip() {
    Assertions.assertEquals(0, instance.skip(1));
  }

  @Test
  public void testAvailable() {
    Assertions.assertEquals(0, instance.available());
  }

  @Test
  public void testClose() {
    try {
      instance.close();
    } catch (Exception e) {
      Assertions.fail(); // This assertion is made to fail the test case in case the close() throws exception
    }
  }

  @Test
  public void testBufferInputStream() {
    Assertions.assertNotNull(instance);
  }

  @Test
  public void testReadBoolean() {
    Assertions.assertFalse(instance.readBoolean());
  }

  @Test
  public void testReadShort() {
    Assertions.assertEquals(0, instance.readShort());
  }

  @Test
  public void testReadInt() {
    Assertions.assertEquals(0, instance.readInt());
  }

  @Test
  public void testReadLong() {
    Assertions.assertEquals(0, instance.readLong());
  }

  @Test
  public void testGetIndex() {
    Assertions.assertEquals(0, instance.getIndex());
  }

  @Test
  public void testReadString() {
    Assertions.assertNotNull(instance.readString());
  }
}
