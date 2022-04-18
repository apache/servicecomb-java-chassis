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
package org.apache.servicecomb.transport.highway;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.client.tcp.AbstractTcpClientPackage;
import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HighwayClientPackage extends AbstractTcpClientPackage {
  private static final Logger LOGGER = LoggerFactory.getLogger(HighwayClientPackage.class);

  private final Invocation invocation;

  private final OperationProtobuf operationProtobuf;

  public HighwayClientPackage(Invocation invocation, OperationProtobuf operationProtobuf, long msRequestTimeout) {
    this.invocation = invocation;
    this.operationProtobuf = operationProtobuf;
    this.setMsRequestTimeout(msRequestTimeout);
  }

  @Override
  public TcpOutputStream createStream() {
    try {
      return HighwayCodec.encodeRequest(msgId, invocation, operationProtobuf);
    } catch (Exception e) {
      String msg = String.format("encode request failed. appid=%s, qualifiedName=%s",
          invocation.getAppId(),
          invocation.getOperationMeta().getMicroserviceQualifiedName());
      LOGGER.error(msg, e);
      throw new Error(msg, e);
    }
  }
}
