/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.transport.highway;

import io.servicecomb.codec.protobuf.definition.OperationProtobuf;
import io.servicecomb.core.Invocation;
import io.servicecomb.foundation.vertx.client.tcp.AbstractTcpClientPackage;
import io.servicecomb.foundation.vertx.tcp.TcpOutputStream;

public class HighwayClientPackage extends AbstractTcpClientPackage {
  private Invocation invocation;

  private OperationProtobuf operationProtobuf;

  private HighwayClientConnection tcpClient;

  public HighwayClientPackage(Invocation invocation, OperationProtobuf operationProtobuf,
      HighwayClientConnection tcpClient) {
    this.invocation = invocation;
    this.operationProtobuf = operationProtobuf;
    this.tcpClient = tcpClient;
  }

  @Override
  public TcpOutputStream createStream() {
    try {
      return HighwayCodec.encodeRequest(msgId, invocation, operationProtobuf, tcpClient.getProtobufFeature());
    } catch (Exception e) {
      String msg = String.format("encode request failed. appid=%s, qualifiedName=%s",
          invocation.getAppId(),
          invocation.getOperationMeta().getMicroserviceQualifiedName());
      throw new Error(msg, e);
    }
  }
}
