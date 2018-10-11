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

import java.util.Map;

import org.apache.servicecomb.codec.protobuf.definition.ProtobufManager;
import org.apache.servicecomb.codec.protobuf.utils.WrapSchema;
import org.apache.servicecomb.swagger.invocation.response.Headers;

import io.protostuff.ProtobufOutput;
import io.protostuff.Tag;
import io.vertx.core.buffer.Buffer;

public class ResponseHeader {
  private static WrapSchema responseHeaderSchema = ProtobufManager.getDefaultScopedProtobufSchemaManager()
      .getOrCreateSchema(ResponseHeader.class);

  public static WrapSchema getResponseHeaderSchema() {
    return responseHeaderSchema;
  }

  public static ResponseHeader readObject(Buffer bodyBuffer) throws Exception {
    return responseHeaderSchema.readObject(bodyBuffer);
  }

  // 运行时必须的数据，比如body是否压缩
  // 预留特性选项
  //CHECKSTYLE:OFF: magicnumber
  @Tag(5)
  private int flags;

  @Tag(1)
  private int statusCode;

  @Tag(2)
  private String reason;

  @Tag(3)
  private Map<String, String> context;

  @Tag(4)
  private Headers headers = new Headers();

  //CHECKSTYLE:ON: magicnumber
  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getReasonPhrase() {
    return reason;
  }

  public void setReasonPhrase(String reason) {
    this.reason = reason;
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

  public void writeObject(ProtobufOutput output) throws Exception {
    responseHeaderSchema.writeObject(output, this);
  }
}
