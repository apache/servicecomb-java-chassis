/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.servlet;

import java.io.IOException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.servicecomb.foundation.vertx.stream.BufferInputStream;

public class CachedHttpServletRequest extends HttpServletRequestWrapper {
  private ServletInputStream inputStream;

  public CachedHttpServletRequest(HttpServletRequest request) {
    super(request);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (this.inputStream == null) {
      byte inputBytes[] = IOUtils.toByteArray(getRequest().getInputStream());
      ByteBuf byteBuf = Unpooled.wrappedBuffer(inputBytes);
      this.inputStream = new BufferInputStream(byteBuf);;
    }
    return this.inputStream;
  }
}
