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
import java.util.List;
import java.util.Map.Entry;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.response.Headers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

/**
 * cse应答在transport层已经完成了码流到对象的转换
 * 这里是适配springmvc的机制，让调用者能拿到已经转换好的对象
 */
public class CseClientHttpResponse implements ClientHttpResponse {
  // 让springmvc client以为应答有body
  // mark、reset都是有锁的，这里通过重写取消了锁
  private static final InputStream BODY_INPUT_STREAM = new InputStream() {
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

    public void reset() throws IOException {
    }
  };

  private Response response;

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
      Headers headers = response.getHeaders();
      if (headers.getHeaderMap() != null) {
        for (Entry<String, List<Object>> entry : headers.getHeaderMap().entrySet()) {
          for (Object value : entry.getValue()) {
            tmpHeaders.add(entry.getKey(), String.valueOf(value));
          }
        }
      }

      httpHeaders = tmpHeaders;
    }
    return httpHeaders;
  }

  @Override
  public HttpStatus getStatusCode() throws IOException {
    // TODO:springmvc不允许自定义http错误码
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
