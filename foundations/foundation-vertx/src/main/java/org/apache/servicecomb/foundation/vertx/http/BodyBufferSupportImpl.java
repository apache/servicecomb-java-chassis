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

package org.apache.servicecomb.foundation.vertx.http;

import org.apache.servicecomb.foundation.vertx.VertxUtils;

import io.vertx.core.buffer.Buffer;

public class BodyBufferSupportImpl implements BodyBufferSupport {
  protected Buffer bodyBuffer;

  private byte[] bodyBytes;

  private int bodyLength;

  @Override
  public void setBodyBuffer(Buffer bodyBuffer) {
    this.bodyBuffer = bodyBuffer;
    this.bodyBytes = null;
    this.bodyLength = 0;
  }

  private void prepare() {
    if (bodyBytes == null && bodyBuffer != null) {
      bodyLength = bodyBuffer.length();
      bodyBytes = VertxUtils.getBytesFast(bodyBuffer);
    }
  }


  @Override
  public Buffer getBodyBuffer() {
    return bodyBuffer;
  }

  @Override
  public byte[] getBodyBytes() {
    prepare();
    return bodyBytes;
  }

  @Override
  public int getBodyBytesLength() {
    prepare();
    return bodyLength;
  }
}
