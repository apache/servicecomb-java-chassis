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

package org.apache.servicecomb.core;

import org.apache.servicecomb.registry.api.DiscoveryInstance;
import org.apache.servicecomb.registry.discovery.StatefulDiscoveryInstance;

/**
 * Internal representation for microservice instance address.
 */
public class Endpoint {
  // Registry address: http://192.168.1.1:8080
  // see: http://www.ietf.org/rfc/rfc2396.txt
  private final String endpoint;

  private final Transport transport;

  private final DiscoveryInstance instance;

  // Internal address format recognized by Transport
  private final Object address;

  public Endpoint(Transport transport, String endpoint) {
    this(transport, endpoint, null);
  }

  public Endpoint(Transport transport, String endpoint, DiscoveryInstance instance) {
    this.transport = transport;
    this.endpoint = endpoint;
    this.instance = instance;
    this.address = transport.parseAddress(this.endpoint);
  }

  public Endpoint(Transport transport, String endpoint, StatefulDiscoveryInstance instance, Object address) {
    this.transport = transport;
    this.endpoint = endpoint;
    this.instance = instance;
    this.address = address;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public DiscoveryInstance getMicroserviceInstance() {
    return instance;
  }

  public Transport getTransport() {
    return transport;
  }

  public Object getAddress() {
    return address;
  }

  @Override
  public String toString() {
    return endpoint;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Endpoint) {
      return this.endpoint.equals(((Endpoint) o).getEndpoint());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.endpoint.hashCode();
  }
}
