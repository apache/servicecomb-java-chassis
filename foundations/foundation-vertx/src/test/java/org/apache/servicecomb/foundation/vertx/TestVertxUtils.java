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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import javax.xml.ws.Holder;

import org.apache.servicecomb.foundation.vertx.stream.BufferInputStream;
import org.junit.Assert;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;

public class TestVertxUtils {
  @Test
  public void testGetOrCreateVertx() throws InterruptedException {
    Vertx vertx = VertxUtils.getOrCreateVertxByName("ut", null);

    Holder<String> name = new Holder<>();
    CountDownLatch latch = new CountDownLatch(1);
    vertx.runOnContext(v -> {
      name.value = Thread.currentThread().getName();
      latch.countDown();
    });
    latch.await();

    Assert.assertEquals(name.value, "ut-vert.x-eventloop-thread-0");
    VertxUtils.closeVertxByName("ut");
  }

  @Test
  public void testVertxUtilsInitNullOptions() {
    Vertx vertx = VertxUtils.init(null);
    Assert.assertNotEquals(null, vertx);
    vertx.close();
  }

  @Test
  public void testVertxUtilsInitWithOptions() {
    VertxOptions oOptions = new VertxOptions();
    oOptions.setClustered(false);

    Vertx vertx = VertxUtils.init(oOptions);
    Assert.assertNotEquals(null, vertx);
    vertx.close();
  }

  @Test
  public void testgetBytesFastBufferInputStream() throws IOException {
    byte[] bytes = new byte[] {1};
    ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

    try (BufferInputStream inputStream = new BufferInputStream(byteBuf)) {
      byte[] result = VertxUtils.getBytesFast(inputStream);
      Assert.assertSame(bytes, result);
    }
  }

  @Test
  public void testgetBytesFastNormalInputStream() throws IOException {
    byte[] bytes = new byte[] {1};

    try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
      byte[] result = VertxUtils.getBytesFast(inputStream);
      Assert.assertEquals(1, result[0]);
    }
  }

  @Test
  public void testgetBytesFastBuffer() {
    Buffer buffer = Buffer.buffer();
    buffer.appendByte((byte) 1);

    byte[] result = VertxUtils.getBytesFast(buffer);
    Assert.assertEquals(1, result[0]);
  }

  @Test
  public void testgetBytesFastByteBufHasArray() {
    byte[] bytes = new byte[] {1};
    ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

    byte[] result = VertxUtils.getBytesFast(byteBuf);
    Assert.assertSame(bytes, result);
  }

  @Test
  public void testgetBytesFastByteBufCopy() {
    ByteBuf byteBuf = Unpooled.directBuffer();
    byteBuf.writeByte(1);
    Assert.assertFalse(byteBuf.hasArray());

    byte[] result = VertxUtils.getBytesFast(byteBuf);
    Assert.assertEquals(1, result[0]);

    byteBuf.release();
  }
}
