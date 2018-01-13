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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

import io.netty.buffer.ByteBuf;

public class BufferInputStream extends ServletInputStream {
  private ByteBuf byteBuf;

  public BufferInputStream(ByteBuf buffer) {
    this.byteBuf = buffer;
  }

  @Override
  public long skip(long len) {
    int skipLen = Math.min((int) len, available());
    byteBuf.skipBytes(skipLen);
    return skipLen;
  }

  public byte readByte() {
    return byteBuf.readByte();
  }

  @Override
  public int read() {
    return byteBuf.readByte();
  }

  public boolean readBoolean() {
    return byteBuf.readBoolean();
  }

  public short readShort() {
    return byteBuf.readShort();
  }

  public int readInt() {
    return byteBuf.readInt();
  }

  public long readLong() {
    return byteBuf.readLong();
  }

  public int getIndex() {
    return byteBuf.readerIndex();
  }

  public String readString() {
    int length = readInt();
    byte[] bytes = new byte[length];
    byteBuf.readBytes(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  @Override
  public int read(byte[] b) {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) {
    int avail = available();
    if (len > avail) {
      len = avail;
    }

    if (len == 0) {
      return -1;
    }

    byteBuf.readBytes(b, off, len);
    return len;
  }

  @Override
  public int available() {
    return byteBuf.readableBytes();
  }

  @Override
  public void close() {
    byteBuf.release();
  }

  @Override
  public void reset() throws IOException {
    byteBuf.resetReaderIndex();
  }

  public ByteBuf getByteBuf() {
    return byteBuf;
  }

  @Override
  public boolean isFinished() {
    return !byteBuf.isReadable();
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {
  }
}
