package io.servicecomb.authentication;

import io.servicecomb.foundation.token.AuthenticationTokenManager;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class RSAAuthenticationTokenManager implements AuthenticationTokenManager {

	private String privateKey;
	
	private String publicKey;
	
	private String token;
	
	@Override
	public String getToken() {
		return token;
	}

	@Override
	public boolean vaild() {
		
		return true;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	

}
