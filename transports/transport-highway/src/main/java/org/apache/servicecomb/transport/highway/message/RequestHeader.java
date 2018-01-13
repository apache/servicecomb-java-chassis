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

import org.apache.servicecomb.codec.protobuf.utils.ProtobufSchemaUtils;
import org.apache.servicecomb.codec.protobuf.utils.WrapSchema;

import io.protostuff.ProtobufOutput;
import io.protostuff.Tag;
import io.protostuff.runtime.ProtobufFeature;
import io.vertx.core.buffer.Buffer;

/**
 * tag的缺失、乱序，是因为要兼容历史版本
 * 1.tag(4)，是历史版本中的压缩算法名，转移到login消息中传递
 */
public class RequestHeader {
  private static WrapSchema requestHeaderSchema = ProtobufSchemaUtils.getOrCreateSchema(RequestHeader.class);

  public static WrapSchema getRequestHeaderSchema() {
    return requestHeaderSchema;
  }

  public static RequestHeader readObject(Buffer bodyBuffer, ProtobufFeature protobufFeature) throws Exception {
    return requestHeaderSchema.readObject(bodyBuffer, protobufFeature);
  }

  //CHECKSTYLE:OFF: magicnumber
  @Tag(2)
  private byte msgType;

  // 运行时必须的数据，比如body是否压缩
  // 预留特性选项
  @Tag(3)
  private int flags;

  @Tag(1)
  private String destMicroservice;

  @Tag(5)
  private String schemaId;

  @Tag(6)
  private String operationName;

  @Tag(7)
  private Map<String, String> context;

  //CHECKSTYLE:ON
  public byte getMsgType() {
    return msgType;
  }

  public void setMsgType(byte msgType) {
    this.msgType = msgType;
  }

  public String getDestMicroservice() {
    return destMicroservice;
  }

  public void setDestMicroservice(String destMicroservice) {
    this.destMicroservice = destMicroservice;
  }

  public int getFlags() {
    return flags;
  }

  public void setFlags(int flags) {
    this.flags = flags;
  }

  public String getSchemaId() {
    return schemaId;
  }

  public void setSchemaId(String schemaId) {
    this.schemaId = schemaId;
  }

  public String getOperationName() {
    return operationName;
  }

  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }

  public Map<String, String> getContext() {
    return context;
  }

  public void setContext(Map<String, String> context) {
    this.context = context;
  }

  public void writeObject(ProtobufOutput output) throws Exception {
    requestHeaderSchema.writeObject(output, this);
  }
}
