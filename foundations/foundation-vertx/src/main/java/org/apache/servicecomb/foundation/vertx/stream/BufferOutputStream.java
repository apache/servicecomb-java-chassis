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

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

/**
 * BufferOutputStream
 *
 *
 */
public class BufferOutputStream extends OutputStream {
  private static final int DIRECT_BUFFER_SIZE = 1024;

  protected ByteBuf byteBuf;

  private boolean needReleaseBuffer;

  public BufferOutputStream() {
    // TODO:默认大小加配置项
    // TODO:如何与pool配合起来，vertx中默认都是unpool的，我们的阻塞模式下，申请与释放也不在一个线程，估计更用不上？
    //        后续通道没问题了，再来验证这个问题
    //        this(PooledByteBufAllocator.DEFAULT.directBuffer());

    //        this(PooledByteBufAllocator.DEFAULT.directBuffer(DIRECT_BUFFER_SIZE));

    //        this(UnpooledByteBufAllocator.DEFAULT.directBuffer(DIRECT_BUFFER_SIZE));
    //        needReleaseBuffer = false;

    //                this(UnpooledByteBufAllocator.DEFAULT.heapBuffer(DIRECT_BUFFER_SIZE));

    this(Buffer.buffer(DIRECT_BUFFER_SIZE).getByteBuf());
    needReleaseBuffer = false;
  }


  public BufferOutputStream(ByteBuf buffer) {
    this.byteBuf = buffer;
  }

  public ByteBuf getByteBuf() {
    return byteBuf;
  }

  public Buffer getBuffer() {
    return Buffer.buffer(byteBuf);
  }

  public int length() {
    return byteBuf.readableBytes();
  }

  public void writeByte(byte value) {
    byteBuf.writeByte(value);
  }

  // 实际是写byte
  @Override
  public void write(int byteValue) {
    byteBuf.writeByte((byte) byteValue);
  }

  public void write(boolean value) {
    byteBuf.writeBoolean(value);
  }

  public void writeInt(int pos, int value) {
    byteBuf.setInt(pos, value);
  }

  public void writeShort(short value) {
    byteBuf.writeShort(value);
  }

  public void writeInt(int value) {
    byteBuf.writeInt(value);
  }

  public void writeLong(long value) {
    byteBuf.writeLong(value);
  }

  public void writeString(String value) {
    byteBuf.writeInt(value.length());
    byteBuf.writeCharSequence(value, StandardCharsets.UTF_8);
  }

  public void write(byte[] b) {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] bytes, int offset, int len) {
    byteBuf.writeBytes(bytes, offset, len);
  }

  @Override
  public void close() {
    if (needReleaseBuffer && byteBuf != null) {
      byteBuf.release();
    }
  }

  public int writerIndex() {
    return byteBuf.writerIndex();
  }
}
