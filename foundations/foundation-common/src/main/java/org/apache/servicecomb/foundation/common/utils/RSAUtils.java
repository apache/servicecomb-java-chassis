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
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSAUtils {

  private final static Logger LOGGER = LoggerFactory.getLogger(RSAUtils.class);

  private final static String RSA_ALG = "RSA";

  private final static String SIGN_ALG = "SHA256withRSA";

  private final static int KEY_SIZE = 2048;

  private static Base64.Encoder encoder = Base64.getEncoder();

  private static Base64.Decoder decoder = Base64.getDecoder();

  private static KeyFactory kf = null;

  static {

    try {
      kf = KeyFactory.getInstance(RSA_ALG);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("init keyfactory error");
    }
  }

  public static RSAKeyPairEntry generateRSAKeyPair() {
    try {
      KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(RSA_ALG);
      keyGenerator.initialize(KEY_SIZE, new SecureRandom());
      KeyPair keyPair = keyGenerator.generateKeyPair();
      PublicKey pubKey = keyPair.getPublic();
      PrivateKey privKey = keyPair.getPrivate();
      return new RSAKeyPairEntry(privKey, pubKey, encoder.encodeToString(pubKey.getEncoded()));
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("generate rsa keypair faild");
      throw new IllegalStateException("perhaps error occurred on jre");
    }
  }

  /**
   * if has performance problem ,change Signature to ThreadLocal instance 
   */
  public static String sign(String content, PrivateKey privateKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
    Signature signature = Signature.getInstance(SIGN_ALG);
    signature.initSign(privateKey);
    signature.update(content.getBytes());
    byte[] signByte = signature.sign();
    return encoder.encodeToString(signByte);
  }

  /**
   * 
   * if has performance problem ,change Signature to ThreadLocal instance  
   * @param publicKey public key after base64 encode 
   * @param sign 签名
   * @param content original content 
   * @return verify result
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws InvalidKeyException
   * @throws SignatureException
   */
  public static boolean verify(String publicKey, String sign, String content)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    if (null == kf) {
      throw new NoSuchAlgorithmException(RSA_ALG + " KeyFactory not available");
    }
    byte[] bytes = decoder.decode(publicKey);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
    PublicKey pubKey = kf.generatePublic(keySpec);
    Signature signature = Signature.getInstance(SIGN_ALG);
    signature.initVerify(pubKey);
    signature.update(content.getBytes());
    return signature.verify(decoder.decode(sign));
  }

}
