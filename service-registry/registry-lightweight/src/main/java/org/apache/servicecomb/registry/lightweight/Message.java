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

package org.apache.servicecomb.registry.lightweight;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import io.vertx.core.json.jackson.DatabindCodec;

public class Message<T> {
  public static <T> Message<T> of(MessageType type, T body) {
    return new Message<T>()
        .setType(type)
        .setBody(body);
  }

  private MessageType type;

  @JsonTypeInfo(use = Id.NAME, property = "type", include = As.EXTERNAL_PROPERTY)
  @JsonSubTypes(
      {
          @Type(name = "REGISTER", value = RegisterRequest.class),
          @Type(name = "UNREGISTER", value = UnregisterRequest.class)
      }
  )
  private T body;

  public MessageType getType() {
    return type;
  }

  public Message<T> setType(MessageType type) {
    this.type = type;
    return this;
  }

  public T getBody() {
    return body;
  }

  public Message<T> setBody(T body) {
    this.body = body;
    return this;
  }

  public byte[] encode() throws IOException {
    return DatabindCodec.mapper().writeValueAsBytes(this);
  }

  public static Message<?> decode(byte[] bytes, int length) throws IOException {
    return DatabindCodec.mapper()
        .readValue(bytes, 0, length, Message.class);
  }
}
