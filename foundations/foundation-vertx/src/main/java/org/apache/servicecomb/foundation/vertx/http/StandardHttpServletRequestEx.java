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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.servicecomb.foundation.vertx.stream.BufferInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;

public class StandardHttpServletRequestEx extends HttpServletRequestWrapper implements HttpServletRequestEx {
  private BodyBufferSupport bodyBuffer = new BodyBufferSupportImpl();

  private boolean cacheRequest;

  private ServletInputStream inputStream;

  // by servlet specification
  // only parse application/x-www-form-urlencoded of post request automatically
  // we will parse this even not post method
  private Map<String, String[]> parameterMap;

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

  private Map<String, String[]> parseParameterMap() {
    // 1.post method already parsed by servlet
    // 2.not APPLICATION_FORM_URLENCODED, no need to enhance
    if (getMethod().equalsIgnoreCase(HttpMethod.POST)
        || !StringUtils.startsWithIgnoreCase(getContentType(), MediaType.APPLICATION_FORM_URLENCODED)) {
      return super.getParameterMap();
    }

    Map<String, List<String>> listMap = parseUrlEncodedBody();
    mergeParameterMaptoListMap(listMap);
    return convertListMapToArrayMap(listMap);
  }

  private Map<String, String[]> convertListMapToArrayMap(Map<String, List<String>> listMap) {
    Map<String, String[]> arrayMap = new HashMap<>();
    for (Entry<String, List<String>> entry : listMap.entrySet()) {
      arrayMap.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
    }
    return arrayMap;
  }

  private void mergeParameterMaptoListMap(Map<String, List<String>> listMap) {
    for (Entry<String, String[]> entry : super.getParameterMap().entrySet()) {
      List<String> values = listMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
      // follow servlet behavior, inherited value first, and then body value
      values.addAll(0, Arrays.asList(entry.getValue()));
    }
  }

  private Map<String, List<String>> parseUrlEncodedBody() {
    try (InputStream inputStream = getInputStream()) {
      Map<String, List<String>> listMap = new HashMap<>();
      String body = IOUtils.toString(inputStream);
      List<NameValuePair> pairs = URLEncodedUtils
          .parse(body, getCharacterEncoding() == null ? null : Charset.forName(getCharacterEncoding()));
      for (NameValuePair pair : pairs) {
        List<String> values = listMap.computeIfAbsent(pair.getName(), k -> new ArrayList<>());
        values.add(pair.getValue());
      }
      return listMap;
    } catch (IOException e) {
      throw new IllegalStateException("", e);
    }
  }

  @Override
  public String[] getParameterValues(String name) {
    return getParameterMap().get(name);
  }

  @Override
  public String getParameter(String name) {
    String[] values = getParameterMap().get(name);
    return values == null ? null : values[0];
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return Collections.enumeration(getParameterMap().keySet());
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    if (parameterMap == null) {
      parameterMap = parseParameterMap();
    }

    return parameterMap;
  }

  @Override
  public void setParameter(String name, String value) {
    getParameterMap().put(name, new String[] {value});
  }
}
