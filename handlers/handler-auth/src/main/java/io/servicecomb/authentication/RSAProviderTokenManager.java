package io.servicecomb.authentication;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.AuthenticationTokenManager;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.MicroserviceInstanceCache;

public class RSAProviderTokenManager implements AuthenticationTokenManager{

	
	@Override
	public boolean vaild(String token) {
 
        try {
        	RSAAuthenticationToken rsaToken = RSAAuthenticationToken.fromStr(token);
            String sign = rsaToken.getSign();
            String content = rsaToken.plainToken();
            String publicKey =  getPublicKey(rsaToken.getInstanceId(), rsaToken.getServiceId());
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

	private String getPublicKey(String instanceId, String serviceId) {
		Optional<MicroserviceInstance> instances = Optional.of(MicroserviceInstanceCache.getOrCreate(serviceId, instanceId));
		return instances.map(MicroserviceInstance :: getProperties).map(new Function< Map<String, String>, String>() {

			@Override
			public String apply(Map<String, String> properties) {
				return properties.get(Const.INSTANCE_PUBKEY_PRO);
			}
			
		}).get();
	}

}
