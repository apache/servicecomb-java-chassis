package io.servicecomb.authentication;

import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.AuthenticationTokenManager;
import io.servicecomb.foundation.token.RSAKeypair4Auth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

public class RSAProviderTokenManager implements AuthenticationTokenManager{

	
	@Override
	public boolean vaild(String token) {
 
        try {
        	RSAAuthenticationToken rsaToken = RSAAuthenticationToken.fromStr(token);
            String sign = rsaToken.getSign();
            String content = rsaToken.plainToken();
            String publicKey =  getPublicKeyByInstanceId(rsaToken.getInstanceId());
            boolean verify = RSAUtils.verify(publicKey, sign, content);
            if (verify)
            {
            	long generateTime = rsaToken.getGenerateTime();
            	Date expiredDate = new Date(generateTime + RSAAuthenticationToken.TOKEN_ACTIVE_TIME + 15 * 60 * 1000);
            	Date now = new Date();
            	if (now.before(expiredDate))
            	{
            		return true;
            	}
            }
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| InvalidKeySpecException | SignatureException e) {

            return false;
		}
		return false;
	}

	private String getPublicKeyByInstanceId(String instanceId) {
		//TODO get from cache
		return RSAKeypair4Auth.INSTANCE.getPublicKey();
	}

}
