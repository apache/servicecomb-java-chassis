package io.servicecomb.foundation.common.utils;

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

public class RSAUtils {

  private final static String RSA_ALG = "RSA";

  private final static String SIGN_ALG = "SHA256withRSA";
  
  private final static int KEY_SIZE = 2048;

  private static Base64.Encoder encoder = Base64.getEncoder();

  private static Base64.Decoder decoder = Base64.getDecoder();

  public static RSAKeyPairEntry getRSAKeyPair() {
    try {
      KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(RSA_ALG);
      keyGenerator.initialize(KEY_SIZE, new SecureRandom());
      KeyPair keyPair = keyGenerator.generateKeyPair();
      PublicKey pubKey = keyPair.getPublic();
      PrivateKey privKey = keyPair.getPrivate();
      return new RSAKeyPairEntry(privKey, pubKey, encoder.encodeToString(pubKey.getEncoded()));
    } catch (NoSuchAlgorithmException e) {
      throw new Error(e);
    }
  }

  public static String sign(String content, PrivateKey privateKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
    Signature signature = Signature.getInstance(SIGN_ALG);
    signature.initSign(privateKey);
    signature.update(content.getBytes());
    byte[] signByte = signature.sign();
    return encoder.encodeToString(signByte);
  }

  public static boolean verify(String publicKey, String sign, String content)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    byte[] bytes = decoder.decode(publicKey);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
    KeyFactory kf = KeyFactory.getInstance(RSA_ALG);
    PublicKey pubKey = kf.generatePublic(keySpec);
    Signature signature = Signature.getInstance(SIGN_ALG);
    signature.initVerify(pubKey);
    signature.update(content.getBytes());
    return signature.verify(decoder.decode(sign));
  }

}
