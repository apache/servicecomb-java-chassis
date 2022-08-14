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
import org.junit.Before;
import org.junit.Test;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

public class CertificateUtilTest {
  static class MyX509Certificate extends X509Certificate {
    private static final long serialVersionUID = -3585440601605666278L;

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
  public void testGetCN() {

    MyX509Certificate xxmyX509Certificate = Mockito.spy(new MyX509Certificate());
    X500Principal aX500Principal = Mockito.mock(X500Principal.class);
    Mockito.doReturn(aX500Principal).when(xxmyX509Certificate).getSubjectX500Principal();
    Mockito.when(aX500Principal.getName()).thenReturn("CN=Test1234");
    Set<String> strExpect = CertificateUtil.getCN(xxmyX509Certificate);

    Assertions.assertTrue(strExpect.contains("Test1234"));
  }

  @Test
  public void testGetCNException() {
    MyX509Certificate xxmyX509Certificate = Mockito.spy(new MyX509Certificate());
    X500Principal aX500Principal = Mockito.mock(X500Principal.class);
    Mockito.doReturn(aX500Principal).when(xxmyX509Certificate).getSubjectX500Principal();
    Mockito.when(aX500Principal.getName()).thenReturn("NOCN=Test1234");

    try {
      Set<String> strExpect = CertificateUtil.getCN(xxmyX509Certificate);
      Assertions.assertEquals(strExpect.size(), 0);
    } catch (IllegalArgumentException e) {
      Assertions.assertNotNull(null);
    }
  }

  @Test
  public void testFindOwner() {
    MyX509Certificate myX509Certificate1 = Mockito.spy(new MyX509Certificate());
    MyX509Certificate myX509Certificate2 = Mockito.spy(new MyX509Certificate());
    X500Principal aX500Principal1 = Mockito.mock(X500Principal.class);
    X500Principal aX500Principal2 = Mockito.mock(X500Principal.class);
    Mockito.doReturn(aX500Principal1).when(myX509Certificate1).getSubjectX500Principal();
    Mockito.doReturn(aX500Principal2).when(myX509Certificate1).getIssuerX500Principal();
    Mockito.doReturn(aX500Principal1).when(myX509Certificate2).getSubjectX500Principal();
    Mockito.doReturn(aX500Principal2).when(myX509Certificate2).getIssuerX500Principal();
    Mockito.when(aX500Principal1.getName()).thenReturn("Huawei");
    Mockito.when(aX500Principal2.getName()).thenReturn("Huawei");

    MyX509Certificate[] xxmyX509Certificate = new MyX509Certificate[2];
    xxmyX509Certificate[0] = myX509Certificate1;
    xxmyX509Certificate[1] = myX509Certificate2;

    X509Certificate aX509Certificate = CertificateUtil.findOwner(xxmyX509Certificate);
    Assertions.assertNull(aX509Certificate);
  }

  @Test
  public void testFindRootCAException() {
    MyX509Certificate myX509Certificate1 = Mockito.spy(new MyX509Certificate());
    MyX509Certificate myX509Certificate2 = Mockito.spy(new MyX509Certificate());
    X500Principal aX500Principal1 = Mockito.mock(X500Principal.class);
    X500Principal aX500Principal2 = Mockito.mock(X500Principal.class);
    Mockito.doReturn(aX500Principal1).when(myX509Certificate1).getSubjectX500Principal();
    Mockito.doReturn(aX500Principal2).when(myX509Certificate1).getIssuerX500Principal();
    Mockito.doReturn(aX500Principal1).when(myX509Certificate2).getSubjectX500Principal();
    Mockito.doReturn(aX500Principal2).when(myX509Certificate2).getIssuerX500Principal();
    Mockito.when(aX500Principal1.getName()).thenReturn("Huawei1");
    Mockito.when(aX500Principal2.getName()).thenReturn("Huawei3");

    MyX509Certificate[] xxmyX509Certificate = new MyX509Certificate[2];
    xxmyX509Certificate[0] = myX509Certificate1;
    xxmyX509Certificate[1] = myX509Certificate2;

    try {
      X509Certificate aX509Certificate = CertificateUtil.findOwner(xxmyX509Certificate);
      Assertions.assertNull(aX509Certificate);
    } catch (IllegalArgumentException e) {
      Assertions.assertEquals("bad certificate chain: no root CA.", e.getMessage());
    }
  }
}
