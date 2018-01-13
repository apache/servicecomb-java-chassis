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

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.foundation.vertx.stream.BufferInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;

public class StandardHttpServletRequestEx extends HttpServletRequestWrapper implements HttpServletRequestEx {
  private BodyBufferSupport bodyBuffer = new BodyBufferSupportImpl();

  private boolean cacheRequest;

  private ServletInputStream inputStream;

  public StandardHttpServletRequestEx(HttpServletRequest request) {
    super(request);
  }

  public void setCacheRequest(boolean cacheRequest) {
    this.cacheRequest = cacheRequest;
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (this.inputStream == null) {
      if (cacheRequest) {
        byte inputBytes[] = IOUtils.toByteArray(getRequest().getInputStream());
        ByteBuf byteBuf = Unpooled.wrappedBuffer(inputBytes);
        this.inputStream = new BufferInputStream(byteBuf);
        setBodyBuffer(Buffer.buffer(Unpooled.wrappedBuffer(byteBuf)));
      } else {
        this.inputStream = getRequest().getInputStream();
      }
    }
    return this.inputStream;
  }

  @Override
  public void setBodyBuffer(Buffer bodyBuffer) {
    this.bodyBuffer.setBodyBuffer(bodyBuffer);
  }

  @Override
  public Buffer getBodyBuffer() {
    return bodyBuffer.getBodyBuffer();
  }

  @Override
  public byte[] getBodyBytes() {
    return bodyBuffer.getBodyBytes();
  }

  @Override
  public int getBodyBytesLength() {
    return bodyBuffer.getBodyBytesLength();
  }
}
