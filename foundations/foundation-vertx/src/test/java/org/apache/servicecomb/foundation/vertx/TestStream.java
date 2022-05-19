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

package org.apache.servicecomb.foundation.vertx;

import org.apache.servicecomb.foundation.vertx.stream.BufferInputStream;
import org.apache.servicecomb.foundation.vertx.stream.BufferOutputStream;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStream {

  private static final int DIRECT_BUFFER_SIZE = 1024;

  @Test
  public void testBufferInputStream() {
    ByteBuf obuf = Buffer.buffer(DIRECT_BUFFER_SIZE).getByteBuf();
    obuf.writeBytes(("testss").getBytes());
    @SuppressWarnings("resource")
    BufferInputStream oBufferInputStream = new BufferInputStream(obuf);
    Assertions.assertNotEquals(1234, oBufferInputStream.skip(0));
    Assertions.assertNotEquals(obuf.readByte(), oBufferInputStream.readByte());
    Assertions.assertEquals(obuf.readByte() + 1, oBufferInputStream.read());
    Assertions.assertEquals(obuf.readBoolean(), oBufferInputStream.readBoolean());
    Assertions.assertEquals(obuf.readerIndex(), oBufferInputStream.getIndex());
    Assertions.assertEquals(obuf.readableBytes(), oBufferInputStream.available());
  }

  @Test
  public void testBufferOutputStream() {
    @SuppressWarnings({"resource"})
    BufferOutputStream oBufferOutputStream = new BufferOutputStream();
    oBufferOutputStream.writeString("test");
    oBufferOutputStream.write(1);
    oBufferOutputStream.write(true);
    Assertions.assertTrue((1 < oBufferOutputStream.length()));
  }
}
