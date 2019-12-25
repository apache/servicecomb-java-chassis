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

import org.apache.servicecomb.foundation.protobuf.ProtoMapperFactory;
import org.apache.servicecomb.foundation.protobuf.RootDeserializer;
import org.apache.servicecomb.foundation.protobuf.RootSerializer;

import io.vertx.core.buffer.Buffer;

public class LoginRequest {
  // TODO : WEAK refactor reuse
  private static ProtoMapperFactory protoMapperFactory = new ProtoMapperFactory();

  private static RootDeserializer<LoginRequest> rootDeserializer = protoMapperFactory.createFromName("LoginRequest.proto")
      .createRootDeserializer("LoginRequest", LoginRequest.class);

  private static RootSerializer rootSerializer = protoMapperFactory.createFromName("LoginRequest.proto")
      .createRootSerializer("LoginRequest", LoginRequest.class);

  public static RootSerializer getRootSerializer() {
    return rootSerializer;
  }

  public static LoginRequest readObject(Buffer bodyBuffer) throws Exception {
    return rootDeserializer.deserialize(bodyBuffer.getBytes());
  }

  private String protocol;

  // 压缩算法名字
  private String zipName;

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getZipName() {
    return zipName;
  }

  public void setZipName(String zipName) {
    this.zipName = zipName;
  }

}
