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

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.junit.Assert;
import org.junit.Test;

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
      int count = 1;

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
      int count = 1;

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
    serverSocket.bind(new InetSocketAddress("127.0.0.1", 8886));
    String[] protos = serverSocket.getEnabledCipherSuites();
    String[] protosExpected =
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA"
            .split(",");
    Assert.assertArrayEquals(protos, protosExpected);
    String[] ciphers = serverSocket.getEnabledCipherSuites();
    String[] ciphersExpected =
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA"
            .split(",");
    Assert.assertArrayEquals(ciphers, ciphersExpected);
    Assert.assertEquals(serverSocket.getNeedClientAuth(), true);

    SSLOption clientoption = SSLOption.build(DIR + "/client.ssl.properties");
    SSLSocket clientsocket = SSLManager.createSSLSocket(clientoption, custom);
    String[] clientprotos = clientsocket.getEnabledCipherSuites();
    String[] clientprotosExpected =
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA"
            .split(",");
    Assert.assertArrayEquals(clientprotos, clientprotosExpected);
    String[] clientciphers = clientsocket.getEnabledCipherSuites();
    String[] clientciphersExpected =
        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_DSS_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_DHE_RSA_WITH_AES_128_CBC_SHA,TLS_DHE_DSS_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA"
            .split(",");
    Assert.assertArrayEquals(clientciphers, clientciphersExpected);
    Assert.assertEquals(clientsocket.getNeedClientAuth(), false);
    boolean validAssert = true;
    try {
      clientsocket.connect(new InetSocketAddress("127.0.0.1", 8886));

      new Thread() {
        public void run() {

          try {
            SSLSocket s = (SSLSocket) serverSocket.accept();
            s.addHandshakeCompletedListener(new HandshakeCompletedListener() {

              @Override
              public void handshakeCompleted(HandshakeCompletedEvent arg0) {

              }
            });
            s.getOutputStream().write(new byte[] {0, 1});
          } catch (IOException e) {
            e.printStackTrace();
            // this should not happen, do a false assert
            Assert.assertEquals(false, true);
          }
        }
      }.start();

      clientsocket.startHandshake();
      clientsocket.close();
      serverSocket.close();

      // socked successfully opened and closed
    } catch (Exception e) {
      e.printStackTrace();
      validAssert = false;
    }
    Assert.assertTrue(validAssert);
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
    Assert.assertEquals(false, aSSLEngine.getUseClientMode());
    Assert.assertNotNull(aSSLEngine);
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
    Assert.assertNotNull(aSSLEngine);
    Assert.assertEquals("host1", aSSLEngine.getPeerHost().toString());
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
      public final SSLContext getInstance(String type) throws NoSuchAlgorithmException {
        throw new NoSuchAlgorithmException();
      }
    };

    try {
      SSLContext context = SSLManager.createSSLContext(option, custom);
      Assert.assertNotNull(context);
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
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
      public final SSLContext getInstance(String type) throws KeyManagementException {
        throw new KeyManagementException();
      }
    };

    try {
      SSLContext context = SSLManager.createSSLContext(option, custom);
      Assert.assertNotNull(context);
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
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
      public final SSLContext getInstance(String type) throws UnknownHostException {
        throw new UnknownHostException();
      }
    };

    try {
      SSLServerSocket context = SSLManager.createSSLServerSocket(option, custom);

      Assert.assertNotNull(context);
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
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
      public final SSLContext getInstance(String type) throws IOException {
        throw new IOException();
      }
    };

    try {
      SSLServerSocket context = SSLManager.createSSLServerSocket(option, custom);
      Assert.assertNotNull(context);
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
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
      public final SSLContext getInstance(String type) throws UnknownHostException {
        throw new UnknownHostException();
      }
    };

    try {
      SSLSocket context = SSLManager.createSSLSocket(option, custom);
      Assert.assertNotNull(context);
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
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
      public final SSLContext getInstance(String type) throws IOException {
        throw new IOException();
      }
    };

    try {
      SSLSocket context = SSLManager.createSSLSocket(option, custom);
      Assert.assertNotNull(context);
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
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
    Assert.assertNotNull(aSSLSocketFactory.getDefaultCipherSuites()[0]);
  }

  public void testGetSupportedCiphers() {
    String[] ciphers = SSLManager.getEnalbedCiphers("TLS_RSA_WITH_AES_128_GCM_SHA256");
    Assert.assertEquals(ciphers[0], "TLS_RSA_WITH_AES_128_GCM_SHA256");
  }
}
