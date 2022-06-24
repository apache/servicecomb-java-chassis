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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public final class KeyStoreUtil {
  private KeyStoreUtil() {

  }

  public static KeyStore createKeyStore(String storeName, String storeType,
      char[] storeValue) {
    if (storeName == null) {
      return null;
    }

    File storeFile = new File(storeName);

    try {
      if (storeFile.isFile()) {
        return createKeyStore(new FileInputStream(storeFile), storeType, storeValue);
      }

      ClassLoader classLoader =
          Thread.currentThread().getContextClassLoader() == null ? KeyStoreUtil.class.getClassLoader()
              : Thread.currentThread().getContextClassLoader();
      URL resource = classLoader.getResource(storeName);
      if (resource != null) {
        return createKeyStore(resource.openStream(), storeType, storeValue);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Bad key store or value."
          + e.getMessage());
    }

    return null;
  }

  public static KeyStore createKeyStore(InputStream store, String storeType,
      char[] storeValue) {
    try (InputStream is = store) {
      KeyStore keystore = KeyStore.getInstance(storeType);
      keystore.load(is, storeValue);
      return keystore;
    } catch (Exception e) {
      throw new IllegalArgumentException("Bad key store or value."
          + e.getMessage());
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public static CRL[] createCRL(String crlfile) {
    InputStream is = null;
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      is = new FileInputStream(crlfile);
      Collection c = cf.generateCRLs(is);
      return (CRL[]) c.toArray(new CRL[0]);
    } catch (CertificateException e) {
      throw new IllegalArgumentException("bad cert file.");
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("crl file not found.");
    } catch (CRLException e) {
      throw new IllegalArgumentException("bad crl file.");
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          ignore();
        }
      }
    }
  }

  public static KeyManager[] createKeyManagers(final KeyStore keystore,
      char[] keyvalue) {
    try {
      KeyManagerFactory kmfactory =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmfactory.init(keystore, keyvalue);
      return kmfactory.getKeyManagers();
    } catch (Exception e) {
      throw new IllegalArgumentException("Bad key store."
          + e.getMessage());
    }
  }

  public static TrustManager[] createTrustManagers(final KeyStore keystore) {
    try {
      TrustManagerFactory tmfactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmfactory.init(keystore);
      return tmfactory.getTrustManagers();
    } catch (Exception e) {
      throw new IllegalArgumentException("Bad trust store."
          + e.getMessage());
    }
  }

  private static void ignore() {
  }
}
