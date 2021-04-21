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

package org.apache.servicecomb.http.client.common;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SSLSocketFactoryExt extends SSLSocketFactory {
  private SSLSocketFactory sslSocketFactory;

  private String host;

  private int port;

  public SSLSocketFactoryExt(SSLSocketFactory factory, String host, int port) {
    this.sslSocketFactory = factory;
    this.host = host;
    this.port = port;
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return this.sslSocketFactory.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return this.sslSocketFactory.getSupportedCipherSuites();
  }

  @Override
  public Socket createSocket() throws IOException {
    return this.sslSocketFactory.createSocket(host, port);
  }

  @Override
  public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
    return this.sslSocketFactory.createSocket(socket, host, port, autoClose);
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return this.sslSocketFactory.createSocket(host, port);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException,
      UnknownHostException {
    return this.sslSocketFactory.createSocket(host, port, localHost, localPort);
  }

  @Override
  public Socket createSocket(InetAddress localHost, int port) throws IOException {
    return this.sslSocketFactory.createSocket(localHost, port);
  }

  @Override
  public Socket createSocket(InetAddress localHost, int localPort, InetAddress localHost1, int localPort1)
      throws IOException {
    return this.sslSocketFactory.createSocket(localHost, localPort, localHost1, localPort1);
  }
}
