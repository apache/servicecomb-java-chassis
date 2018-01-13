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
import org.junit.Assert;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

public class TestStream {

  private static final int DIRECT_BUFFER_SIZE = 1024;

  @Test
  public void testBufferInputStream() {
    ByteBuf obuf = Buffer.buffer(DIRECT_BUFFER_SIZE).getByteBuf();
    obuf.writeBytes(("testss").getBytes());
    @SuppressWarnings("resource")
    BufferInputStream oBufferInputStream = new BufferInputStream(obuf);
    Assert.assertNotEquals(1234, oBufferInputStream.skip(0));
    Assert.assertNotEquals(obuf.readByte(), oBufferInputStream.readByte());
    Assert.assertEquals(obuf.readByte() + 1, oBufferInputStream.read());
    Assert.assertEquals(obuf.readBoolean(), oBufferInputStream.readBoolean());
    Assert.assertEquals(obuf.readerIndex(), oBufferInputStream.getIndex());
    Assert.assertEquals(obuf.readableBytes(), oBufferInputStream.available());
    Assert.assertNotEquals(null, oBufferInputStream.read(("test").getBytes()));
  }

  @Test
  public void testBufferOutputStream() {
    @SuppressWarnings({"resource"})
    BufferOutputStream oBufferOutputStream = new BufferOutputStream();
    oBufferOutputStream.writeString("test");
    Assert.assertNotEquals(null, oBufferOutputStream.writerIndex());
    oBufferOutputStream.write(1);
    oBufferOutputStream.write(true);
    Assert.assertEquals(true, (1 < oBufferOutputStream.length()));
  }
}
