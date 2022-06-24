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

import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;

import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestTcpParser {
  long msgId;

  Buffer headerBuffer;

  Buffer bodyBuffer;

  @Test
  public void test() throws UnsupportedEncodingException {
    TcpBufferHandler output = (_msgId, _headerBuffer, _bodyBuffer) -> {
      msgId = _msgId;
      headerBuffer = _headerBuffer;
      bodyBuffer = _bodyBuffer;
    };

    byte[] header = new byte[] {1, 2, 3};
    byte[] body = new byte[] {1, 2, 3, 4};
    TcpOutputStream os = new TcpOutputStream(1);
    os.writeInt(header.length + body.length);
    os.writeInt(header.length);
    os.write(header);
    os.write(body);

    TcpParser parser = new TcpParser(output);
    parser.handle(os.getBuffer());
    os.close();

    Assertions.assertEquals(1, msgId);
    Assertions.assertArrayEquals(header, headerBuffer.getBytes());
    Assertions.assertArrayEquals(body, bodyBuffer.getBytes());
  }
}
