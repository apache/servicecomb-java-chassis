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
package org.apache.servicecomb.foundation.common.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Assert;
import org.junit.Test;

public class TestRSAUtil {

  @Test
  public void testSignVerify()
      throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
    RSAKeyPairEntry rsaKeyPairEntry = RSAUtils.generateRSAKeyPair();

    Assert.assertNotNull(rsaKeyPairEntry.getPublicKeyEncoded());
    Assert.assertNotNull(rsaKeyPairEntry.getPrivateKey());
    Assert.assertNotNull(rsaKeyPairEntry.getPublicKey());
    String testContent = "instance-id@201711201930@randomstr";
    String signstr = RSAUtils.sign(testContent, rsaKeyPairEntry.getPrivateKey());
    Assert.assertTrue(RSAUtils.verify(rsaKeyPairEntry.getPublicKeyEncoded(), signstr, testContent));

  }

  @Test
  public void testSignVerify2()
      throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException {
    String sign =
        "WBYouF6hXYrXzBA31HC3VX8Bw9PNgJUtVqOPAaeW9ye3q/D7WWb0M+XMouBIWxWY6v9Un1dGu5Rkjlx6gZbnlHkb2VO8qFR3Y6lppooWCirzpvEBRjlJQu8LPBur0BCfYGq8XYrEZA2NU6sg2zXieqCSiX6BnMnBHNn4cR9iZpk=";
    String content =
        "e8a04b54cf2711e7b701286ed488fc20@c8636e5acf1f11e7b701286ed488fc20@1511315597475@9t0tp8ce80SUM5ts6iRGjFJMvCdQ7uvhpyh0RM7smKm3p4wYOrojr4oT1Pnwx7xwgcgEFbQdwPJxIMfivpQ1rHGqiLp67cjACvJ3Ke39pmeAVhybsLADfid6oSjscFaJ";
    String pubKey =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxKl5TNUTec7fL2degQcCk6vKf3c0wsfNK5V6elKzjWxm0MwbRj/UeR20VSnicBmVIOWrBS9LiERPPvjmmWUOSS2vxwr5XfhBhZ07gCAUNxBOTzgMo5nE45DhhZu5Jzt5qSV6o10Kq7+fCCBlDZ1UoWxZceHkUt5AxcrhEDulFjQIDAQAB";
    Assert.assertTrue(RSAUtils.verify(pubKey, sign, content));

  }

}
