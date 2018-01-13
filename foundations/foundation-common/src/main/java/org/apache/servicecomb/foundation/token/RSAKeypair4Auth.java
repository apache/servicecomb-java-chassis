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

package org.apache.servicecomb.foundation.token;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 进程级别公私钥对
 *
 */
public class RSAKeypair4Auth {

  private RSAKeypair4Auth() {
  }

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
