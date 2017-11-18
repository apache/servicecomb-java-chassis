package io.servicecomb;

import io.servicecomb.authentication.RSAAuthenticationTokenManager;
import io.servicecomb.core.BootListener;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.AuthenticationTokenManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthHandlerBoot implements BootListener {
	
	@Autowired
	private AuthenticationTokenManager authenticationToken;

	@Override
	public void onBootEvent(BootEvent event) {
		if (EventType.BEFORE_REGISTRY.equals(event.getEventType()) && authenticationToken instanceof RSAAuthenticationTokenManager)
		{
			String []privAndPubKey = RSAUtils.getEncodedKeyPair();
			RSAAuthenticationTokenManager token = (RSAAuthenticationTokenManager)authenticationToken;
			token.setPrivateKey(privAndPubKey[0]);
			token.setPublicKey(privAndPubKey[1]);
		}

	}

	public void setAuthenticationToken(AuthenticationTokenManager authenticationToken) {
		this.authenticationToken = authenticationToken;
	}
	
	

}
