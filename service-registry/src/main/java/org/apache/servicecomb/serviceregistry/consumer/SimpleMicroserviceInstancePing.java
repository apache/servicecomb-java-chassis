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
package org.apache.servicecomb.serviceregistry.consumer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.servicecomb.foundation.common.net.IpPort;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

/**
 * Simple implementation of .MicroserviceInstancePing using telnet
 */
public class SimpleMicroserviceInstancePing implements MicroserviceInstancePing {
  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  public boolean ping(MicroserviceInstance instance) {
    if (instance.getEndpoints() != null && instance.getEndpoints().size() > 0) {
      IpPort ipPort = NetUtils.parseIpPortFromURI(instance.getEndpoints().get(0));
      try (Socket s = new Socket()) {
        s.connect(new InetSocketAddress(ipPort.getHostOrIp(), ipPort.getPort()), 3000);
        return true;
      } catch (IOException e) {
        // ignore this error
      }
    }
    return false;
  }
}
