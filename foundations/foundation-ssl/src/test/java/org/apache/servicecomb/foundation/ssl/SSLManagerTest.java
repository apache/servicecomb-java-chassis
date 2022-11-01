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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class SSLManagerTest {
  private final String DIR = Thread.currentThread().getContextClassLoader().getResource("").getPath();

  @Test
  public void testSSLManagerServerAndClient(final @Mocked NetworkInterface nif) throws Exception {
    final InetAddress ia = Inet4Address.getByName("10.57.65.225");
    final Enumeration<NetworkInterface> interfaces = new Enumeration<NetworkInterface>() {
      final int count = 1;

      int cur = 0;

      @Override
      public boolean hasMoreElements() {
        if (cur < count) {
          cur++;
          return true;
        }
        return false;
      }

      @Override
      public NetworkInterface nextElement() {
        return nif;
      }
    };

    final Enumeration<InetAddress> ias = new Enumeration<InetAddress>() {
      final int count = 1;

      int cur = 0;

      @Override
      public boolean hasMoreElements() {
        if (cur < count) {
          cur++;
          return true;
        }
        return false;
      }

      @Override
      public InetAddress nextElement() {
        return ia;
      }
    };

    new Expectations() {
      @Mocked
      NetworkInterface nif;

      {
        NetworkInterface.getNetworkInterfaces();
        result = interfaces;
      }
    };
    new Expectations() {
      {
        nif.getInetAddresses();
        result = ias;
        ia.getHostAddress();
        result = "10.57.65.225";
      }
    };

    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");
    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };
    final SSLServerSocket serverSocket = SSLManager.createSSLServerSocket(option, custom);
    Assertions.assertTrue(serverSocket.getNeedClientAuth());
    serverSocket.bind(new InetSocketAddress("127.0.0.1", 8886));
    String[] protos = serverSocket.getEnabledCipherSuites();
    String[] protosExpected =
        "TLS_AES_128_GCM_SHA256,TLS_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA"
            .split(",");
    Assertions.assertArrayEquals(protos, protosExpected);
    String[] ciphers = serverSocket.getEnabledCipherSuites();
    String[] ciphersExpected =
        "TLS_AES_128_GCM_SHA256,TLS_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA"
            .split(",");
    Assertions.assertArrayEquals(ciphers, ciphersExpected);
    Assertions.assertTrue(serverSocket.getNeedClientAuth());

    SSLOption clientoption = SSLOption.build(DIR + "/client.ssl.properties");
    SSLSocket clientsocket = SSLManager.createSSLSocket(clientoption, custom);
    String[] clientprotos = clientsocket.getEnabledCipherSuites();
    String[] clientprotosExpected =
        "TLS_AES_128_GCM_SHA256,TLS_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA"
            .split(",");
    Assertions.assertArrayEquals(clientprotos, clientprotosExpected);
    String[] clientciphers = clientsocket.getEnabledCipherSuites();
    String[] clientciphersExpected =
        "TLS_AES_128_GCM_SHA256,TLS_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA"
            .split(",");
    Assertions.assertArrayEquals(clientciphers, clientciphersExpected);
    Assertions.assertFalse(clientsocket.getNeedClientAuth());
    boolean validAssert = true;
    try {
      clientsocket.connect(new InetSocketAddress("127.0.0.1", 8886));

      new Thread(() -> {

        try {
          SSLSocket s = (SSLSocket) serverSocket.accept();
          s.addHandshakeCompletedListener(arg0 -> {

          });
          s.getOutputStream().write(new byte[] {0, 1});
        } catch (IOException e) {
          e.printStackTrace();
           Assertions.fail("this should not happen");
        }
      }).start();

      clientsocket.startHandshake();
      clientsocket.close();
      serverSocket.close();

      // socked successfully opened and closed
    } catch (Exception e) {
      e.printStackTrace();
      validAssert = false;
    }
    Assertions.assertTrue(validAssert);
  }

  @Test
  public void testCreateSSLEngine() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");
    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    SSLEngine aSSLEngine = SSLManager.createSSLEngine(option, custom);
    // if client mode may not decided at initialization. Different JDK is different, do not check it.
    // Assertions.assertEquals(false, aSSLEngine.getUseClientMode());
    Assertions.assertTrue(aSSLEngine.getNeedClientAuth());
  }

  @Test
  public void testCreateSSLEngineClientAuthNone() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");
    option.setClientAuth("NONE");
    option.setAuthPeer(false);
    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    SSLEngine aSSLEngine = SSLManager.createSSLEngine(option, custom);
    // if client mode may not decided at initialization. Different JDK is different, do not check it.
    // Assertions.assertEquals(false, aSSLEngine.getUseClientMode());
    Assertions.assertFalse(aSSLEngine.getNeedClientAuth());
    Assertions.assertFalse(aSSLEngine.getWantClientAuth());
  }

  @Test
  public void testCreateSSLEnginewithPort() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");
    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    int port = 39093;
    String peerHost = "host1";
    SSLEngine aSSLEngine = SSLManager.createSSLEngine(option, custom, peerHost, port);
    Assertions.assertNotNull(aSSLEngine);
    Assertions.assertEquals("host1", aSSLEngine.getPeerHost());
  }

  @Test
  public void testCreateSSLContextResource() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.resource.properties");

    SSLCustom custom = SSLCustom.defaultSSLCustom();

    SSLContext context = SSLManager.createSSLContext(option, custom);
    Assertions.assertNotNull(context);
  }

  @Test
  public void testCreateSSLContextException() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");

    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    new MockUp<SSLContext>() {
      @Mock
      public SSLContext getInstance(String type) throws NoSuchAlgorithmException {
        throw new NoSuchAlgorithmException();
      }
    };

    try {
      SSLManager.createSSLContext(option, custom);
      Assertions.assertNotNull(null);
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
  }

  @Test
  public void testCreateSSLContextKeyManagementException() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");

    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    new MockUp<SSLContext>() {
      @Mock
      public SSLContext getInstance(String type) throws KeyManagementException {
        throw new KeyManagementException();
      }
    };

    try {
      SSLManager.createSSLContext(option, custom);
      Assertions.assertNotNull(null);
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
  }

  @Test
  public void testCreateSSLServerSocketException() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");

    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    new MockUp<SSLContext>() {
      @Mock
      public SSLContext getInstance(String type) throws UnknownHostException {
        throw new UnknownHostException();
      }
    };

    try {
      SSLManager.createSSLServerSocket(option, custom);

      Assertions.assertNotNull(null);
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
  }

  @Test
  public void testCreateSSLServerSocketIOException() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");

    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    new MockUp<SSLContext>() {
      @Mock
      public SSLContext getInstance(String type) throws IOException {
        throw new IOException();
      }
    };

    try {
      SSLManager.createSSLServerSocket(option, custom);
      Assertions.assertNotNull(null);
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
  }

  @Test
  public void testCreateSSLSocketException() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");

    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    new MockUp<SSLContext>() {
      @Mock
      public SSLContext getInstance(String type) throws UnknownHostException {
        throw new UnknownHostException();
      }
    };

    try {
      SSLManager.createSSLSocket(option, custom);
      Assertions.assertNotNull(null);
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
  }

  @Test
  public void testCreateSSLSocketIOException() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");

    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    new MockUp<SSLContext>() {
      @Mock
      public SSLContext getInstance(String type) throws IOException {
        throw new IOException();
      }
    };

    try {
      SSLManager.createSSLSocket(option, custom);
      Assertions.assertNotNull(null);
    } catch (Exception e) {
      Assertions.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
  }

  @Test
  public void testCreateSSLSocketFactory() {
    SSLOption option = SSLOption.build(DIR + "/server.ssl.properties");
    SSLCustom custom = new SSLCustom() {
      @Override
      public String getFullPath(String filename) {
        return DIR + "/ssl/" + filename;
      }

      @Override
      public char[] decode(char[] encrypted) {
        return encrypted;
      }
    };

    SSLSocketFactory aSSLSocketFactory = SSLManager.createSSLSocketFactory(option, custom);
    Assertions.assertNotNull(aSSLSocketFactory.getDefaultCipherSuites()[0]);
  }

  @Test
  public void testGetSupportedCiphers() {
    SSLOption option = new SSLOption();
    option.setCiphers("TLS_RSA_WITH_AES_128_GCM_SHA256");
    option.setProtocols("TLSv1.2");
    String[] ciphers = SSLManager.getEnabledCiphers(option);
    Assertions.assertEquals(ciphers[0], "TLS_RSA_WITH_AES_128_GCM_SHA256");
  }
}
