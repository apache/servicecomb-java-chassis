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

import io.vertx.core.buffer.Buffer;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

public class BufferInputStream extends ServletInputStream {
  private int readIndex = 0;

  private final Buffer byteBuf;

  public BufferInputStream(Buffer buffer) {
    this.byteBuf = buffer;
  }

  @Override
  public long skip(long len) {
    int skipLen = Math.min((int) len, available());
    this.readIndex += skipLen;
    return skipLen;
  }

  public byte readByte() {
    int index = readIndex;
    readIndex = index + 1;
    return byteBuf.getByte(index);
  }

  @Override
  public int read() {
    return this.readByte() & 255;
  }

  public boolean readBoolean() {
    return this.readByte() != 0;
  }

  public short readShort() {
    int index = readIndex;
    readIndex = index + 2;
    return byteBuf.getShort(index);
  }

  public int readInt() {
    int index = readIndex;
    readIndex = index + 4;
    return byteBuf.getInt(index);
  }

  public long readLong() {
    int index = readIndex;
    readIndex = index + 8;
    return byteBuf.getLong(index);
  }

  public int getIndex() {
    return readIndex;
  }

  public String readString() {
    int length = readInt();
    byte[] bytes = new byte[length];
    int index = readIndex;
    readIndex = index + length;
    byteBuf.getBytes(index, readIndex, bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  @Override
  public int read(byte[] b) {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) {
    int avail = available();
    if (avail <= 0) {
      return -1;
    }

    if (len == 0) {
      return 0;
    }

    if (len > avail) {
      len = avail;
    }

    int index = readIndex;
    readIndex = index + len;

    byteBuf.getBytes(index, readIndex, b);
    return len;
  }

  @Override
  public int available() {
    return byteBuf.length() - readIndex;
  }

  @Override
  public void close() {
    // nothing to do
  }

  @Override
  public void reset() throws IOException {
    readIndex = 0;
  }

  public Buffer getByteBuf() {
    return byteBuf;
  }

  @Override
  public boolean isFinished() {
    return byteBuf.length() > readIndex;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {
  }
}
