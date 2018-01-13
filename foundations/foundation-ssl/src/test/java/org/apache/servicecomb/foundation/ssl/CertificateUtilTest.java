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

import java.math.BigInteger;
import java.security.InvalidKeyException;
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

import javax.security.auth.x500.X500Principal;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class CertificateUtilTest {
  class MyX509Certificate extends X509Certificate {
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

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGetCN(@Mocked X500Principal aX500Principal, @Mocked MyX509Certificate myX509Certificate) {
    new Expectations() {
      {
        aX500Principal.getName();
        result = "CN=Test1234";
        myX509Certificate.getSubjectX500Principal();
        result = aX500Principal;
      }
    };

    MyX509Certificate xxmyX509Certificate = new MyX509Certificate();
    Set<String> strExpect = CertificateUtil.getCN(xxmyX509Certificate);

    Assert.assertEquals(true, strExpect.contains("Test1234"));
  }

  @Test
  public void testGetCNException(@Mocked X500Principal aX500Principal,
      @Mocked MyX509Certificate myX509Certificate) {
    new Expectations() {
      {
        aX500Principal.getName();
        result = "NOCN=Test1234";
        myX509Certificate.getSubjectX500Principal();
        result = aX500Principal;
      }
    };

    MyX509Certificate xxmyX509Certificate = new MyX509Certificate();

    try {
      Set<String> strExpect = CertificateUtil.getCN(xxmyX509Certificate);
      Assert.assertEquals(strExpect.size(), 0);
    } catch (IllegalArgumentException e) {
      Assert.assertNotNull(null);
    }
  }

  @Test
  public void testFindOwner(@Mocked X500Principal aX500Principal1, @Mocked X500Principal aX500Principal2,
      @Mocked MyX509Certificate myX509Certificate) {
    new Expectations() {
      {
        aX500Principal1.getName();
        result = "Huawei";
      }

      {
        aX500Principal2.getName();
        result = "Huawei";
      }

      {
        myX509Certificate.getSubjectX500Principal();
        result = aX500Principal1;

        myX509Certificate.getIssuerX500Principal();
        result = aX500Principal2;
      }
    };

    MyX509Certificate myX509Certificate1 = new MyX509Certificate();
    MyX509Certificate myX509Certificate2 = new MyX509Certificate();

    MyX509Certificate[] xxmyX509Certificate = new MyX509Certificate[2];
    xxmyX509Certificate[0] = myX509Certificate1;
    xxmyX509Certificate[1] = myX509Certificate2;

    X509Certificate aX509Certificate = CertificateUtil.findOwner(xxmyX509Certificate);
    Assert.assertNull(aX509Certificate);
  }

  @Test
  public void testFindRootCAException(@Mocked X500Principal aX500Principal1, @Mocked X500Principal aX500Principal2,
      @Mocked MyX509Certificate myX509Certificate) {
    new Expectations() {
      {
        aX500Principal1.getName();
        result = "Huawei1";
      }

      {
        aX500Principal2.getName();
        result = "Huawei3";
      }

      {
        myX509Certificate.getSubjectX500Principal();
        result = aX500Principal1;

        myX509Certificate.getIssuerX500Principal();
        result = aX500Principal2;
      }
    };

    MyX509Certificate myX509Certificate1 = new MyX509Certificate();
    MyX509Certificate myX509Certificate2 = new MyX509Certificate();

    MyX509Certificate[] xxmyX509Certificate = new MyX509Certificate[2];
    xxmyX509Certificate[0] = myX509Certificate1;
    xxmyX509Certificate[1] = myX509Certificate2;

    try {
      X509Certificate aX509Certificate = CertificateUtil.findOwner(xxmyX509Certificate);
      Assert.assertNull(aX509Certificate);
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("bad certificate chain: no root CA.", e.getMessage());
    }
  }
}
