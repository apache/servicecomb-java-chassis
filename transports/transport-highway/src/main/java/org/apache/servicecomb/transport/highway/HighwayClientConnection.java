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

import org.apache.servicecomb.foundation.vertx.client.tcp.AbstractTcpClientPackage;
import org.apache.servicecomb.foundation.vertx.client.tcp.NetClientWrapper;
import org.apache.servicecomb.foundation.vertx.client.tcp.TcpClientConnection;
import org.apache.servicecomb.foundation.vertx.tcp.TcpOutputStream;
import org.apache.servicecomb.transport.highway.message.LoginRequest;
import org.apache.servicecomb.transport.highway.message.LoginResponse;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.protostuff.runtime.ProtobufFeature;
import io.vertx.core.Context;
import io.vertx.core.buffer.Buffer;

public class HighwayClientConnection extends TcpClientConnection {
  private static final Logger LOGGER = LoggerFactory.getLogger(HighwayClientConnection.class);

  private ProtobufFeature protobufFeature = new ProtobufFeature();

  public HighwayClientConnection(Context context, NetClientWrapper netClientWrapper, String endpoint) {
    super(context, netClientWrapper, endpoint);
    setLocalSupportLogin(true);
  }

  public ProtobufFeature getProtobufFeature() {
    return protobufFeature;
  }

  @Override
  protected TcpOutputStream createLogin() {
    try {
      RequestHeader header = new RequestHeader();
      header.setMsgType(MsgType.LOGIN);

      LoginRequest login = new LoginRequest();
      login.setProtocol(HighwayTransport.NAME);
      login.setUseProtobufMapCodec(true);

      HighwayOutputStream os = new HighwayOutputStream(AbstractTcpClientPackage.getAndIncRequestId(), null);
      os.write(header, LoginRequest.getLoginRequestSchema(), login);
      return os;
    } catch (Throwable e) {
      throw new Error("impossible.", e);
    }
  }

  @Override
  protected boolean onLoginResponse(Buffer bodyBuffer) {
    try {
      LoginResponse response = LoginResponse.readObject(bodyBuffer);
      protobufFeature.setUseProtobufMapCodec(response.isUseProtobufMapCodec());
      return true;
    } catch (Throwable e) {
      LOGGER.error("decode login response failed.", e);
      return false;
    }
  }
}
