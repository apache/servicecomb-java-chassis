package io.servicecomb.foundation.token;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 进程级别公私钥对
 *
 */
public class RSAKeypair4Auth {

  private RSAKeypair4Auth() {
  };

  private PrivateKey privateKey;

  private PublicKey publicKey;
  
  private String publicKeyEncoded;


  public PrivateKey getPrivateKey() {
    return privateKey;
  }


  public void setPrivateKey(PrivateKey privateKey) {
    this.privateKey = privateKey;
  }


  public PublicKey getPublicKey() {
    return publicKey;
  }


  public void setPublicKey(PublicKey publicKey) {
    this.publicKey = publicKey;
  }


  public String getPublicKeyEncoded() {
    return publicKeyEncoded;
  }


  public void setPublicKeyEncoded(String publicKeyEncoded) {
    this.publicKeyEncoded = publicKeyEncoded;
  }

  public static RSAKeypair4Auth INSTANCE = new RSAKeypair4Auth();
}
