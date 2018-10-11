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

package org.apache.servicecomb.transport.common;

import org.apache.servicecomb.codec.protobuf.definition.OperationProtobuf;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.transport.highway.HighwayCodec;
import org.apache.servicecomb.transport.highway.HighwayConfig;
import org.apache.servicecomb.transport.highway.message.RequestHeader;
import org.mockito.Mockito;

import io.vertx.core.buffer.Buffer;
import mockit.Mock;
import mockit.MockUp;

public class MockUtil {

  private static MockUtil instance = new MockUtil();

  private MockUtil() {

  }

  public static MockUtil getInstance() {
    return instance;
  }

  public void mockHighwayConfig() {

    new MockUp<HighwayConfig>() {
      @Mock
      String getAddress() {
        return "127.0.0.1";
      }
    };
  }

  public RequestHeader requestHeader = new RequestHeader();

  public boolean decodeRequestSucc = true;

  public void mockHighwayCodec() {

    new MockUp<HighwayCodec>() {
      @Mock
      RequestHeader readRequestHeader(Buffer headerBuffer) throws Exception {
        return requestHeader;
      }

      @Mock
      public Invocation decodeRequest(RequestHeader header, OperationProtobuf operationProtobuf, Buffer bodyBuffer)
          throws Exception {
        if (decodeRequestSucc) {
          return Mockito.mock(Invocation.class);
        }

        throw new Error("decode failed");
      }
    };
  }
}
