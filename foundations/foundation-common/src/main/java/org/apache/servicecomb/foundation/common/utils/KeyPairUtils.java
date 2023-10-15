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

import java.nio.charset.StandardCharsets;
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

import org.apache.servicecomb.foundation.common.LegacyPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyPairUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyPairUtils.class);

  private static final String KEY_GENERATOR_ALGORITHM = LegacyPropertyFactory.getStringProperty(
      "servicecomb.publicKey.accessControl.keyGeneratorAlgorithm", "RSA");

  private static final String SIGN_ALG = LegacyPropertyFactory.getStringProperty(
      "servicecomb.publicKey.accessControl.signAlgorithm", "SHA256withRSA");

  private static final int KEY_SIZE = LegacyPropertyFactory.getIntProperty(
      "servicecomb.publicKey.accessControl.keySize", 2048);

  private static final Base64.Encoder encoder = Base64.getEncoder();

  private static final Base64.Decoder decoder = Base64.getDecoder();

  private static KeyFactory kf = null;

  static {

    try {
      kf = KeyFactory.getInstance(KEY_GENERATOR_ALGORITHM);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("init keyfactory error");
    }
  }

  public static KeyPairEntry generateALGKeyPair() {
    try {
      KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(KEY_GENERATOR_ALGORITHM);
      keyGenerator.initialize(KEY_SIZE, new SecureRandom());
      KeyPair keyPair = keyGenerator.generateKeyPair();
      PublicKey pubKey = keyPair.getPublic();
      PrivateKey privKey = keyPair.getPrivate();
      return new KeyPairEntry(privKey, pubKey, encoder.encodeToString(pubKey.getEncoded()));
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("generate rsa keypair faild");
      throw new IllegalStateException("perhaps error occurred on jre");
    }
  }

  /**
   * if has performance problem ,change Signature to ThreadLocal instance
   */
  public static String sign(String content, PrivateKey privateKey)
      throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
    Signature signature = Signature.getInstance(SIGN_ALG);
    signature.initSign(privateKey);
    signature.update(content.getBytes(StandardCharsets.UTF_8));
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
      throw new NoSuchAlgorithmException(KEY_GENERATOR_ALGORITHM + " KeyFactory not available");
    }
    byte[] bytes = decoder.decode(publicKey);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
    PublicKey pubKey = kf.generatePublic(keySpec);
    Signature signature = Signature.getInstance(SIGN_ALG);
    signature.initVerify(pubKey);
    signature.update(content.getBytes(StandardCharsets.UTF_8));
    return signature.verify(decoder.decode(sign));
  }
}
