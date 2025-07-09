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

import io.vertx.core.buffer.Buffer;

/**
 * BufferOutputStream.
 *
 * Notice: will not release the underlining ByteBuffer, the user is responsible to release it.
 */
public class BufferOutputStream extends OutputStream {
  private static final int DIRECT_BUFFER_SIZE = 1024;

  protected Buffer byteBuf;

  public BufferOutputStream() {
    this(Buffer.buffer(DIRECT_BUFFER_SIZE));
  }

  public BufferOutputStream(Buffer buffer) {
    this.byteBuf = buffer;
  }

  public Buffer getBuffer() {
    return byteBuf;
  }

  public int length() {
    return byteBuf.length();
  }

  public void writeByte(byte value) {
    byteBuf.appendByte(value);
  }

  // 实际是写byte
  @Override
  public void write(int byteValue) {
    byteBuf.appendByte((byte) byteValue);
  }

  public void write(boolean value) {
    byteBuf.appendByte(value ? (byte) 1 : (byte) 0);
  }

  public void writeInt(int pos, int value) {
    byteBuf.setInt(pos, value);
  }

  public void writeShort(short value) {
    byteBuf.appendShort(value);
  }

  public void writeInt(int value) {
    byteBuf.appendInt(value);
  }

  public void writeLong(long value) {
    byteBuf.appendLong(value);
  }

  public void writeString(String value) {
    writeInt(value.length());
    byteBuf.appendString(value, StandardCharsets.UTF_8.toString());
  }

  @Override
  public void write(byte[] b) {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] bytes, int offset, int len) {
    byteBuf.appendBytes(bytes, offset, len);
  }

  @Override
  public void close() {
    // Do no release byteBuf, the target BufferedInputStream will release it.
  }

  public int writerIndex() {
    return byteBuf.length();
  }
}
