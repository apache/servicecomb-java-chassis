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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import mockit.Mock;
import mockit.MockUp;

public class KeyStoreUtilTest {
  final String strFilePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();

  @Test
  public void testCreateKeyStoreException() {
    String storename = "";
    String storetype = "testType";
    char[] storevalue = "Changeme_123".toCharArray();

    try {
      KeyStoreUtil.createKeyStore(storename, storetype, storevalue);
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Bad key store or value.testType not found", e.getMessage());
    }
  }

  @Test
  public void testCreateKeyStoreException2() {
    String storename = strFilePath + "/ssl/trust.jks";
    String storetype = "PKCS12";
    char[] storevalue = "Changeme_123".toCharArray();

    try {
      KeyStoreUtil.createKeyStore(storename, storetype, storevalue);
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Bad key store or value.DerInputStream.getLength(): lengthTag=109, too big.",
          e.getMessage());
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void testCreateKeyManagersException() {
    KeyStore keystore;
    char[] keyvalue;

    String storename = strFilePath + "/ssl/server.p12";
    String storetype = "PKCS12";
    char[] storevalue = "Changeme_123".toCharArray();

    keystore = KeyStoreUtil.createKeyStore(storename, storetype, storevalue);

    char[] storeKeyValue = null;
    try {
      KeyStoreUtil.createKeyManagers(keystore, storeKeyValue);
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Bad key store.Get Key failed: null",
          e.getMessage());
    }
  }

  @Test
  public void testCreateCRL() {
    String crlfile = strFilePath + "/ssl/server.p12";
    mockGenerateCRLs();
    boolean validAssert = true;
    try {
      CRL[] crl = KeyStoreUtil.createCRL(crlfile);
      Assert.assertNull(crl);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertTrue(validAssert);
  }

  @Test
  public void testCreateCRLException() {
    String crlfile = strFilePath + "/ssl/server.p12";
    boolean validAssert = true;
    try {
      new MockUp<CertificateFactory>() {
        @Mock
        public final CertificateFactory getInstance(String type) throws CertificateException {
          throw new CertificateException();
        }
      };

      KeyStoreUtil.createCRL(crlfile);
    } catch (Exception e) {
      validAssert = false;
    }
    Assert.assertFalse(validAssert);
  }

  @Test
  public void testExceptionFileNotFound() {
    String crlfile = strFilePath + "/ssl/server.p12";
    boolean validAssert = true;
    try {
      new MockUp<CertificateFactory>() {
        @Mock
        public final CertificateFactory getInstance(
            String type) throws CertificateException, FileNotFoundException {
          throw new FileNotFoundException();
        }
      };
      KeyStoreUtil.createCRL(crlfile);
    } catch (Exception e) {
      validAssert = false;
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
    Assert.assertFalse(validAssert);
  }

  @Test
  public void testExceptionCRLException() {
    String crlfile = strFilePath + "/ssl/server.p12";
    boolean validAssert = true;
    try {
      new MockUp<CertificateFactory>() {
        @Mock
        public final CertificateFactory getInstance(String type) throws CertificateException, CRLException {
          throw new CRLException();
        }
      };
      KeyStoreUtil.createCRL(crlfile);
    } catch (Exception e) {
      validAssert = false;
      Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getName());
    }
    Assert.assertFalse(validAssert);
  }

  private void mockGenerateCRLs() {
    new MockUp<CertificateFactory>() {
      @SuppressWarnings("unchecked")
      @Mock
      public final Collection<? extends CRL> generateCRLs(InputStream inStream) throws CRLException {
        return Mockito.mock(Collection.class);
      }
    };
  }
}
