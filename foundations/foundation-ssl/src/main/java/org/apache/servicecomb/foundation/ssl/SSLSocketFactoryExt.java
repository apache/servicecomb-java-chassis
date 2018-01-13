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

package org.apache.servicecomb.foundation.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * 扩展SSLSocketFactory，设置算法和协议列表。
 *
 */
public class SSLSocketFactoryExt extends SSLSocketFactory {
  private SSLSocketFactory sslSocketFactory;

  private String[] ciphers;

  private String[] protos;

  public SSLSocketFactoryExt(SSLSocketFactory factory, String[] ciphers, String[] protos) {
    this.sslSocketFactory = factory;
    this.ciphers = ciphers;
    this.protos = protos;
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
    return wrapSocket((SSLSocket) this.sslSocketFactory.createSocket(s, host, port, autoClose));
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
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return wrapSocket((SSLSocket) this.sslSocketFactory.createSocket(host, port));
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return wrapSocket((SSLSocket) this.sslSocketFactory.createSocket(host, port));
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost,
      int localPort) throws IOException, UnknownHostException {
    return wrapSocket((SSLSocket) this.sslSocketFactory.createSocket(host,
        port,
        localHost,
        localPort));
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
      int localPort) throws IOException {
    return wrapSocket((SSLSocket) this.sslSocketFactory.createSocket(address,
        port,
        localAddress,
        localPort));
  }

  private SSLSocket wrapSocket(SSLSocket socket) {
    socket.setEnabledProtocols(protos);
    socket.setEnabledCipherSuites(ciphers);
    return socket;
  }
}
