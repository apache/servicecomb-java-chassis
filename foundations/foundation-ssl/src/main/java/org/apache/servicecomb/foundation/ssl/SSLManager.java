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
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.commons.lang3.StringUtils;


/**
 * 根据传递的SSLOption构造SSL上下文。请参考JSSE获取相关API的层次参考。
 *
 */
public final class SSLManager {
  private SSLManager() {

  }

  public static SSLContext createSSLContext(SSLOption option, SSLCustom custom) {
    try {
      String keyStoreName = custom.getFullPath(option.getKeyStore());
      char[] keyStoreValue = option.getKeyStoreValue() == null ? new char[0] :
          custom.decode(option.getKeyStoreValue().toCharArray());
      KeyStore keyStore =
          KeyStoreUtil.createKeyStore(keyStoreName,
              option.getKeyStoreType(),
              keyStoreValue);

      KeyManager[] keyManager = null;
      if (keyStore != null) {
        keyManager =
            KeyStoreUtil.createKeyManagers(keyStore, keyStoreValue);
      }

      String trustStoreName = custom.getFullPath(option.getTrustStore());
      char[] trustStoreValue = option.getTrustStoreValue() == null ? new char[0] :
          custom.decode(option.getTrustStoreValue().toCharArray());
      KeyStore trustStore =
          KeyStoreUtil.createKeyStore(trustStoreName,
              option.getTrustStoreType(),
              trustStoreValue);

      TrustManager[] trustManager;
      if (trustStore != null) {
        trustManager =
            KeyStoreUtil.createTrustManagers(trustStore);
      } else {
        trustManager = new TrustManager[] {new TrustAllManager()};
      }

      TrustManager[] wrapped = new TrustManager[trustManager.length];
      for (int i = 0; i < trustManager.length; i++) {
        wrapped[i] =
            new TrustManagerExt((X509ExtendedTrustManager) trustManager[i],
                option, custom);
      }

      // ?: ssl context version
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(keyManager, wrapped, new SecureRandom());
      return context;
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException("NoSuchAlgorithmException."
          + e.getMessage());
    } catch (KeyManagementException e) {
      throw new IllegalArgumentException("KeyManagementException."
          + e.getMessage());
    }
  }

  public static SSLSocketFactory createSSLSocketFactory(SSLOption option, SSLCustom custom) {
    SSLContext context = createSSLContext(option, custom);
    SSLSocketFactory factory = context.getSocketFactory();
    String[] supported = factory.getSupportedCipherSuites();
    String[] enabled = option.getCiphers().split(",");
    return new SSLSocketFactoryExt(factory, getEnabledCiphers(supported, enabled),
        option.getProtocols().split(","));
  }

  public static SSLEngine createSSLEngine(SSLOption option, SSLCustom custom) {
    SSLContext context = createSSLContext(option, custom);
    SSLEngine engine =
        context.createSSLEngine();
    engine.setEnabledProtocols(option.getProtocols().split(","));
    String[] supported = engine.getSupportedCipherSuites();
    String[] enabled = option.getCiphers().split(",");
    engine.setEnabledCipherSuites(getEnabledCiphers(supported, enabled));
    setClientAuth(option, engine);
    return engine;
  }

  public static SSLEngine createSSLEngine(SSLOption option, SSLCustom custom, String peerHost, int peerPort) {
    SSLContext context = createSSLContext(option, custom);
    SSLEngine engine =
        context.createSSLEngine(peerHost, peerPort);
    engine.setEnabledProtocols(option.getProtocols().split(","));
    String[] supported = engine.getSupportedCipherSuites();
    String[] enabled = option.getCiphers().split(",");
    engine.setEnabledCipherSuites(getEnabledCiphers(supported, enabled));
    setClientAuth(option, engine);
    return engine;
  }

  private static void setClientAuth(SSLOption option, SSLEngine engine) {
    if (option.isAuthPeer() || ClientAuth.REQUIRED.equals(option.getClientAuth())) {
      engine.setNeedClientAuth(true);
      return;
    }
    if (ClientAuth.NONE.equals(option.getClientAuth())) {
      engine.setNeedClientAuth(false);
      engine.setWantClientAuth(false);
      return;
    }
    engine.setWantClientAuth(true);
  }

  private static void setClientAuth(SSLOption option, SSLServerSocket serverSocket) {
    if (option.isAuthPeer() || ClientAuth.REQUIRED.equals(option.getClientAuth())) {
      serverSocket.setNeedClientAuth(true);
      return;
    }
    if (ClientAuth.NONE.equals(option.getClientAuth())) {
      serverSocket.setNeedClientAuth(false);
      serverSocket.setWantClientAuth(false);
      return;
    }
    serverSocket.setWantClientAuth(true);
  }

  public static SSLServerSocket createSSLServerSocket(SSLOption option,
      SSLCustom custom) {
    try {
      SSLContext context = createSSLContext(option, custom);
      SSLServerSocketFactory factory = context.getServerSocketFactory();
      SSLServerSocket socket =
          (SSLServerSocket) factory.createServerSocket();
      socket.setEnabledProtocols(option.getProtocols().split(","));
      String[] supported = socket.getSupportedCipherSuites();
      String[] enabled = option.getCiphers().split(",");
      socket.setEnabledCipherSuites(getEnabledCiphers(supported, enabled));
      setClientAuth(option, socket);
      return socket;
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("unkown host");
    } catch (IOException e) {
      throw new IllegalArgumentException("unable create socket");
    }
  }

  public static SSLSocket createSSLSocket(SSLOption option, SSLCustom custom) {
    try {
      SSLContext context = createSSLContext(option, custom);
      SSLSocketFactory factory = context.getSocketFactory();
      SSLSocket socket =
          (SSLSocket) factory.createSocket();
      socket.setEnabledProtocols(option.getProtocols().split(","));
      String[] supported = socket.getSupportedCipherSuites();
      String[] enabled = option.getCiphers().split(",");
      socket.setEnabledCipherSuites(getEnabledCiphers(supported, enabled));
      return socket;
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("unknown host");
    } catch (IOException e) {
      throw new IllegalArgumentException("unable create socket");
    }
  }

  private static String[] getEnabledCiphers(String[] supported,
      String[] enabled) {
    String[] result = new String[enabled.length];
    int count = 0;
    for (String e : enabled) {
      for (String s : supported) {
        if (e.equals(s)) {
          result[count++] = e;
          break;
        }
      }
    }

    if (count == 0) {
      throw new IllegalArgumentException("no enabled cipher suits.");
    }

    String[] r = new String[count];
    System.arraycopy(result, 0, r, 0, count);
    return r;
  }

  public static String[] getEnabledCiphers(SSLOption sslOption) {
    SSLOption option = new SSLOption();
    if (StringUtils.isNotEmpty(sslOption.getProtocols())) {
      option.setProtocols(sslOption.getProtocols());
    } else {
      option.setProtocols("TLSv1.2");
    }
    option.setCiphers(sslOption.getCiphers());
    SSLCustom custom = SSLCustom.defaultSSLCustom();
    SSLSocket socket = createSSLSocket(option, custom);
    return socket.getEnabledCipherSuites();
  }
}
