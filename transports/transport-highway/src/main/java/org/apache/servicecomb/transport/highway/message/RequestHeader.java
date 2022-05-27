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

import org.apache.servicecomb.foundation.protobuf.ProtoMapperFactory;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;

import io.vertx.core.buffer.Buffer;

public class RequestHeader {
  private static final ProtoMapperFactory protoMapperFactory = new ProtoMapperFactory();

  private static final RootDeserializer<RequestHeader> rootDeserializer = protoMapperFactory
      .createFromName("RequestHeader.proto")
      .createRootDeserializer("RequestHeader", RequestHeader.class);

  private static final RootSerializer rootSerializer = protoMapperFactory.createFromName("RequestHeader.proto")
      .createRootSerializer("RequestHeader", RequestHeader.class);

  public static RootSerializer getRootSerializer() {
    return rootSerializer;
  }

  public static RequestHeader readObject(Buffer bodyBuffer) throws Exception {
    return rootDeserializer.deserialize(bodyBuffer.getBytes());
  }

  private int msgType;

  // 运行时必须的数据，比如body是否压缩
  // 预留特性选项
  private int flags;

  private String destMicroservice;

  private String schemaId;

  private String operationName;

  private Map<String, String> context;

  public int getMsgType() {
    return msgType;
  }

  public void setMsgType(int msgType) {
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
}
