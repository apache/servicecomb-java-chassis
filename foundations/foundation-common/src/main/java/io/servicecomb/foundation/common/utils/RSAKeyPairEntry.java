package io.servicecomb.foundation.common.utils;

import java.security.PrivateKey;
import java.security.PublicKey;

public final class RSAKeyPairEntry {
  
  private PrivateKey privateKey;

  private PublicKey publicKey;
  
  private String publicKeyEncoded;
  
  public RSAKeyPairEntry(PrivateKey privateKey, PublicKey publicKey, String publicKeyEncoded)
  {
    this.privateKey = privateKey;
    this.publicKey = publicKey;
    this.publicKeyEncoded = publicKeyEncoded;
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public String getPublicKeyEncoded() {
    return publicKeyEncoded;
  }

}
