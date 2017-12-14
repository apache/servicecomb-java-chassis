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
package io.protostuff.runtime;

public class ProtobufFeature {
  // 历史版本中的protoStuff实现的protobuf的map编码与标准的protobuf不兼容
  // 为保持highway的兼容，旧的不兼容编码也要保留
  // 所以这里默认为false
  // 只有LoginRequest/LoginResponse同时为true时，才使用标准protobuf编码
  private boolean useProtobufMapCodec = false;

  public boolean isUseProtobufMapCodec() {
    return useProtobufMapCodec;
  }

  public void setUseProtobufMapCodec(boolean useProtobufMapCodec) {
    this.useProtobufMapCodec = useProtobufMapCodec;
  }
}
