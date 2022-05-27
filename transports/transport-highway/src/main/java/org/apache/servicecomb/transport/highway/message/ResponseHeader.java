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

package org.apache.servicecomb.transport.highway.message;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.foundation.protobuf.ProtoMapperFactory;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

public class ResponseHeader {
  private static final ProtoMapperFactory protoMapperFactory = new ProtoMapperFactory();

  private static final RootDeserializer<ResponseHeader> rootDeserializer = protoMapperFactory
      .createFromName("ResponseHeader.proto")
      .createRootDeserializer("ResponseHeader", ResponseHeader.class);

  private static final RootSerializer rootSerializer = protoMapperFactory.createFromName("ResponseHeader.proto")
      .createRootSerializer("ResponseHeader", ResponseHeader.class);

  public static RootSerializer getRootSerializer() {
    return rootSerializer;
  }

  public static ResponseHeader readObject(Buffer bodyBuffer) throws Exception {
    return rootDeserializer.deserialize(bodyBuffer.getBytes());
  }

  // 运行时必须的数据，比如body是否压缩
  // 预留特性选项
  private int flags;

  private int statusCode;

  private String reasonPhrase;

  private Map<String, String> context;

  private Headers headers = new Headers();

  public int getFlags() {
    return flags;
  }

  public void setFlags(int flags) {
    this.flags = flags;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  public void setReasonPhrase(String reason) {
    this.reasonPhrase = reason;
  }

  public Map<String, String> getContext() {
    return context;
  }

  public void setContext(Map<String, String> context) {
    this.context = context;
  }

  public Headers getHeaders() {
    return headers;
  }

  public void setHeaders(Headers headers) {
    this.headers = headers;
  }

  public void fromMultiMap(MultiMap multiMap) {
    if (multiMap == null) {
      return;
    }

    for (Entry<String, String> entry : multiMap.entries()) {
      headers.addHeader(entry.getKey(), entry.getValue());
    }
  }

  public MultiMap toMultiMap() {
    MultiMap multiMap = MultiMap.caseInsensitiveMultiMap();
    Map<String, List<Object>> headerMap = headers.getHeaderMap();
    if (headerMap == null) {
      return multiMap;
    }

    for (Entry<String, List<Object>> entry : headerMap.entrySet()) {
      String key = entry.getKey();
      for (Object value : entry.getValue()) {
        multiMap.add(key, value.toString());
      }
    }
    return multiMap;
  }
}
