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

package org.apache.servicecomb.provider.springmvc.reference;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import io.vertx.core.MultiMap;

/**
 * cse应答在transport层已经完成了码流到对象的转换
 * 这里是适配springmvc的机制，让调用者能拿到已经转换好的对象
 */
public class CseClientHttpResponse implements ClientHttpResponse {
  // 让springmvc client以为应答有body
  // mark、reset都是有锁的，这里通过重写取消了锁
  private static final InputStream BODY_INPUT_STREAM = new InputStream() {
    @Override
    public boolean markSupported() {
      return true;
    }

    @Override
    public void mark(int readlimit) {
    }

    @Override
    public int read() throws IOException {
      return 0;
    }

    @Override
    public void reset() throws IOException {
    }
  };

  private final Response response;

  private HttpHeaders httpHeaders;

  public CseClientHttpResponse(Response response) {
    this.response = response;
  }

  public Object getResult() {
    return response.getResult();
  }

  @Override
  public InputStream getBody() throws IOException {
    return BODY_INPUT_STREAM;
  }

  @Override
  public HttpHeaders getHeaders() {
    if (httpHeaders == null) {
      HttpHeaders tmpHeaders = new HttpHeaders();
      MultiMap headers = response.getHeaders();
      if (headers != null) {
        for (Entry<String, String> entry : headers.entries()) {
          tmpHeaders.add(entry.getKey(), entry.getValue());
        }
      }

      httpHeaders = tmpHeaders;
    }
    return httpHeaders;
  }

  @Override
  public HttpStatus getStatusCode() throws IOException {
    return HttpStatus.valueOf(response.getStatusCode());
  }

  @Override
  public int getRawStatusCode() throws IOException {
    return response.getStatusCode();
  }

  @Override
  public String getStatusText() throws IOException {
    return response.getReasonPhrase();
  }

  @Override
  public void close() {
  }
}
