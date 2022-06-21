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
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;

public class TrustManagerExtTest {
  final String strFilePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();

  static class MyX509ExtendedTrustManager extends X509ExtendedTrustManager {
    public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString,
        Socket paramSocket) throws CertificateException {
    }

    public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate,
        String paramString) throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString,
        Socket paramSocket) throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate,
        String paramString) throws CertificateException {
    }

    public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString,
        SSLEngine paramSSLEngine) throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString,
        SSLEngine paramSSLEngine) throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }

  static class MyX509Certificate extends X509Certificate {
    private static final long serialVersionUID = -3585440601605666276L;

    public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
    }

    @Override
    public boolean hasUnsupportedCriticalExtension() {
      return false;
    }

    @Override
    public Set<String> getCriticalExtensionOIDs() {
      return null;
    }

    @Override
    public Set<String> getNonCriticalExtensionOIDs() {
      return null;
    }

    @Override
    public byte[] getExtensionValue(String oid) {
      return null;
    }

    @Override
    public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
    }

    @Override
    public int getVersion() {
      return 0;
    }

    @Override
    public BigInteger getSerialNumber() {
      return null;
    }

    @Override
    public Principal getIssuerDN() {
      return null;
    }

    @Override
    public Principal getSubjectDN() {
      return null;
    }

    @Override
    public Date getNotBefore() {
      return null;
    }

    @Override
    public Date getNotAfter() {
      return null;
    }

    @Override
    public byte[] getTBSCertificate() throws CertificateEncodingException {
      return null;
    }

    @Override
    public byte[] getSignature() {
      return null;
    }

    @Override
    public String getSigAlgName() {
      return null;
    }

    @Override
    public String getSigAlgOID() {
      return null;
    }

    @Override
    public byte[] getSigAlgParams() {
      return null;
    }

    @Override
    public boolean[] getIssuerUniqueID() {
      return null;
    }

    @Override
    public boolean[] getSubjectUniqueID() {
      return null;
    }

    @Override
    public boolean[] getKeyUsage() {
      return null;
    }

    @Override
    public int getBasicConstraints() {
      return 0;
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
      return null;
    }

    @Override
    public void verify(PublicKey key)
        throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
        SignatureException {

    }

    @Override
    public void verify(PublicKey key, String sigProvider)
        throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
        SignatureException {
    }

    @Override
    public String toString() {
      return null;
    }

    @Override
    public PublicKey getPublicKey() {
      return null;
    }
  }

  final SSLOption option = SSLOption.build(strFilePath + "/server.ssl.properties");

  final SSLCustom custom = new SSLCustom() {
    @Override
    public String getFullPath(String filename) {
      return strFilePath + "/ssl/" + filename;
    }

    @Override
    public char[] decode(char[] encrypted) {
      return encrypted;
    }

    @Override
    public String getHost() {
      return "10.67.147.115";
    }
  };

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @SuppressWarnings("unused")
  @Test
  public void testConstructor() {
    String keyStoreName = custom.getFullPath(option.getKeyStore());
    char[] keyStoreValue = custom.decode(option.getKeyStoreValue().toCharArray());
    String trustStoreName = custom.getFullPath(option.getTrustStore());
    char[] trustStoreValue =
        custom.decode(option.getTrustStoreValue().toCharArray());
    KeyStore trustStore =
        KeyStoreUtil.createKeyStore(trustStoreName,
            option.getTrustStoreType(),
            trustStoreValue);
    TrustManager[] trustManager = KeyStoreUtil.createTrustManagers(trustStore);

    TrustManagerExt trustManagerExt = new TrustManagerExt((X509ExtendedTrustManager) trustManager[0],
        option, custom);
    Assertions.assertEquals(3, trustManagerExt.getAcceptedIssuers()[0].getVersion());
    Assertions.assertNotNull(trustManagerExt);
  }

  @Test
  public void testConstructorWithParam() {
    MyX509Certificate myX509Certificate1 = new MyX509Certificate();
    MyX509Certificate myX509Certificate2 = new MyX509Certificate();

    MyX509Certificate[] myX509CertificateArray = new MyX509Certificate[2];
    myX509CertificateArray[0] = myX509Certificate1;
    myX509CertificateArray[1] = myX509Certificate2;

    new Expectations(CertificateUtil.class) {
      {
        CertificateUtil.findOwner((X509Certificate[]) any);
        result = any;

        CertificateUtil.getCN((X509Certificate) any);
        result = "10.67.147.115";
      }
    };

    MyX509ExtendedTrustManager myX509ExtendedTrustManager = new MyX509ExtendedTrustManager();
    TrustManagerExt trustManagerExt = new TrustManagerExt(myX509ExtendedTrustManager, option, custom);

    Assertions.assertNotNull(trustManagerExt);
    boolean validAssert = true;
    try {
      trustManagerExt.checkClientTrusted(myX509CertificateArray, "pks");
      trustManagerExt.checkServerTrusted(myX509CertificateArray, "pks");
      trustManagerExt.getAcceptedIssuers();
    } catch (Exception e) {
      validAssert = false;
    }
    Assertions.assertTrue(validAssert);
  }

  @Test
  public void testCheckClientTrusted(@Mocked CertificateUtil certificateUtil) {
    MyX509Certificate myX509Certificate1 = new MyX509Certificate();
    MyX509Certificate myX509Certificate2 = new MyX509Certificate();

    MyX509Certificate[] myX509CertificateArray = new MyX509Certificate[2];
    myX509CertificateArray[0] = myX509Certificate1;
    myX509CertificateArray[1] = myX509Certificate2;

    new Expectations() {
      {
        CertificateUtil.findOwner((X509Certificate[]) any);
        result = any;

        CertificateUtil.getCN((X509Certificate) any);
        result = "10.67.147.115";
      }
    };

    MyX509ExtendedTrustManager myX509ExtendedTrustManager = new MyX509ExtendedTrustManager();
    TrustManagerExt trustManagerExt = new TrustManagerExt(myX509ExtendedTrustManager, option, custom);

    Socket socket = null;
    SSLEngine sslengine = null;
    boolean validAssert = true;
    try {
      trustManagerExt.checkClientTrusted(myX509CertificateArray, "pks", socket);
      trustManagerExt.checkClientTrusted(myX509CertificateArray, "pks", sslengine);
      trustManagerExt.checkServerTrusted(myX509CertificateArray, "pks", socket);
      trustManagerExt.checkServerTrusted(myX509CertificateArray, "pks", sslengine);
    } catch (Exception e) {
      validAssert = false;
    }
    Assertions.assertTrue(validAssert);
  }

  @Test
  public void testCatchException(@Mocked CertificateUtil certificateUtil) {
    MyX509Certificate myX509Certificate1 = new MyX509Certificate();
    MyX509Certificate myX509Certificate2 = new MyX509Certificate();

    MyX509Certificate[] myX509CertificateArray = new MyX509Certificate[2];
    myX509CertificateArray[0] = myX509Certificate1;
    myX509CertificateArray[1] = myX509Certificate2;

    new Expectations() {
      {
        CertificateUtil.findOwner((X509Certificate[]) any);
        result = any;

        CertificateUtil.getCN((X509Certificate) any);
        result = "10.67.147.114";
      }
    };

    MyX509ExtendedTrustManager myX509ExtendedTrustManager = new MyX509ExtendedTrustManager();
    TrustManagerExt trustManagerExt = new TrustManagerExt(myX509ExtendedTrustManager, option, custom);
    boolean validAssert = true;
    try {
      trustManagerExt.checkClientTrusted(myX509CertificateArray, "pks");
    } catch (CertificateException e) {
      Assertions.assertEquals("CN does not match IP: e=[10.67.147.114],t=null", e.getMessage());
      validAssert = false;
    }
    Assertions.assertFalse(validAssert);
  }

  @Test
  public void testCheckClientTrustedException(@Mocked CertificateUtil certificateUtil) {
    MyX509Certificate myX509Certificate1 = new MyX509Certificate();
    MyX509Certificate myX509Certificate2 = new MyX509Certificate();

    MyX509Certificate[] myX509CertificateArray = new MyX509Certificate[2];
    myX509CertificateArray[0] = myX509Certificate1;
    myX509CertificateArray[1] = myX509Certificate2;

    new Expectations() {
      {
        CertificateUtil.findOwner((X509Certificate[]) any);
        result = any;

        CertificateUtil.getCN((X509Certificate) any);
        result = "10.67.147.115";
      }
    };

    MyX509ExtendedTrustManager myX509ExtendedTrustManager = new MyX509ExtendedTrustManager();
    TrustManagerExt trustManagerExt = new TrustManagerExt(myX509ExtendedTrustManager, option, custom);

    Socket socket = null;
    SSLEngine sslengine = null;

    new MockUp<InputStreamReader>() {
      @Mock
      public int read(char[] cbuf) throws IOException {
        throw new IOException();
      }
    };
    boolean validAssert = true;
    try {
      trustManagerExt.checkClientTrusted(myX509CertificateArray, "pks", socket);
      trustManagerExt.checkClientTrusted(myX509CertificateArray, "pks", sslengine);
      trustManagerExt.checkServerTrusted(myX509CertificateArray, "pks", socket);
      trustManagerExt.checkServerTrusted(myX509CertificateArray, "pks", sslengine);
    } catch (Exception e) {
      Assertions.assertEquals("java.security.cert.CertificateException", e.getClass().getName());
      validAssert = false;
    }
    Assertions.assertFalse(validAssert);
  }
}
