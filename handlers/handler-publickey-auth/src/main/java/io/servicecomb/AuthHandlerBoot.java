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
package io.servicecomb;

import io.servicecomb.core.BootListener;
import io.servicecomb.foundation.common.utils.RSAKeyPairEntry;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.RSAKeypair4Auth;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.Const;

import org.springframework.stereotype.Component;

/**
 * 
 * initialize public and private key pair when system boot before registry instance to service center
 * 
 *
 */
@Component
public class AuthHandlerBoot implements BootListener {


  @Override
  public void onBootEvent(BootEvent event) {
    if (EventType.BEFORE_REGISTRY.equals(event.getEventType())) {
      RSAKeyPairEntry rsaKeyPairEntry = RSAUtils.generateRSAKeyPair();
      RSAKeypair4Auth.INSTANCE.setPrivateKey(rsaKeyPairEntry.getPrivateKey());
      RSAKeypair4Auth.INSTANCE.setPublicKey(rsaKeyPairEntry.getPublicKey());
      RSAKeypair4Auth.INSTANCE.setPublicKeyEncoded(rsaKeyPairEntry.getPublicKeyEncoded());
      RegistryUtils.getMicroserviceInstance().getProperties().put(Const.INSTANCE_PUBKEY_PRO, rsaKeyPairEntry.getPublicKeyEncoded());
    }

  }



}
