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

package org.apache.servicecomb.foundation.vertx.server;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * TcpParser
 *
 *
 */
public class TcpParser implements Handler<Buffer> {
  public static final byte[] TCP_MAGIC;

  public static final int TCP_HEADER_LENGTH = 23;

  static {
    try {
      TCP_MAGIC = "CSE.TCP".getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  enum ParseStatus {
    TCP_HEADER,
    TCP_PAYLOAD
  }

  private TcpBufferHandler outputHandler;

  private RecordParser parser;

  private ParseStatus status;

  private long msgId;

  // 仅仅是header + body，不包括headerLen本身
  private int totalLen;

  private int headerLen;

  public TcpParser(TcpBufferHandler output) {
    this.outputHandler = output;

    reset();
  }

  /**
   * 在解析出错时，通过重新创建parser对象，将整个缓冲区重置
   */
  protected void reset() {
    parser = RecordParser.newFixed(TCP_HEADER_LENGTH, this::onParse);
    status = ParseStatus.TCP_HEADER;

    parser.handle(Buffer.buffer(0));
  }

  public boolean firstNEqual(byte[] a, byte[] b, int n) {
    assert a.length >= n && b.length >= n;

    for (int i = 0; i < n; i++) {
      if (a[i] != b[i]) {
        return false;
      }
    }
    return true;
  }

  protected void onParse(Buffer buffer) {
    switch (status) {
      case TCP_HEADER:
        ByteBuf buf = buffer.getByteBuf();
        if (!firstNEqual(TCP_MAGIC, buf.array(), TCP_MAGIC.length)) {
          reset();
          return;
        }

        buf.skipBytes(TCP_MAGIC.length);
        msgId = buf.readLong();
        totalLen = buf.readInt();
        headerLen = buf.readInt();

        if (totalLen == 0) {
          onReadOnePackage(null, null);
          return;
        }

        parser.fixedSizeMode(totalLen);
        status = ParseStatus.TCP_PAYLOAD;
        break;

      case TCP_PAYLOAD:
        Buffer headerBuffer = buffer.slice(0, headerLen);
        Buffer bodyBuffer = buffer.slice(headerLen, buffer.length());
        onReadOnePackage(headerBuffer, bodyBuffer);
        break;

      default:
        break;
    }
  }

  private void onReadOnePackage(Buffer headerBuffer, Buffer bodyBuffer) {
    outputHandler.handle(msgId, headerBuffer, bodyBuffer);

    parser.fixedSizeMode(TCP_HEADER_LENGTH);
    status = ParseStatus.TCP_HEADER;
  }

  public void handle(Buffer buf) {
    parser.handle(buf);
  }
}
