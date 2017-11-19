package io.servicecomb;

import io.servicecomb.core.BootListener;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.RSAKeypair;

import org.springframework.stereotype.Component;

@Component
public class AuthHandlerBoot implements BootListener {
	

	@Override
	public void onBootEvent(BootEvent event) {
		if (EventType.BEFORE_REGISTRY.equals(event.getEventType()))
		{
			String []privAndPubKey = RSAUtils.getEncodedKeyPair();
			RSAKeypair.INSTANCE.setPrivateKey(privAndPubKey[0]);
			RSAKeypair.INSTANCE.setPublicKey(privAndPubKey[1]);
		}

	}

	
	

}
