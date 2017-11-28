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
      RSAKeyPairEntry rsaKeyPairEntry = RSAUtils.getRSAKeyPair();
      RSAKeypair4Auth.INSTANCE.setPrivateKey(rsaKeyPairEntry.getPrivateKey());
      RSAKeypair4Auth.INSTANCE.setPublicKey(rsaKeyPairEntry.getPublicKey());
      RSAKeypair4Auth.INSTANCE.setPublicKeyEncoded(rsaKeyPairEntry.getPublicKeyEncoded());
      RegistryUtils.getMicroserviceInstance().getProperties().put(Const.INSTANCE_PUBKEY_PRO, rsaKeyPairEntry.getPublicKeyEncoded());
    }

  }



}
