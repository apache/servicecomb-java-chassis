package io.servicecomb.authentication.provider;

import io.servicecomb.authentication.RSAAuthenticationToken;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.AuthenticationTokenManager;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.MicroserviceInstanceCache;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSAProviderTokenManager implements AuthenticationTokenManager {

	private static Logger logger = LoggerFactory.getLogger(RSAProviderTokenManager.class);
	
	@Override
	public boolean vaild(String token) {
		try {
			RSAAuthenticationToken rsaToken = RSAAuthenticationToken.fromStr(token);
			if (null == rsaToken)
			{
				logger.error("token format is error,maybe attack");
				return false;
			}
			String sign = rsaToken.getSign();
			String content = rsaToken.plainToken();
			String publicKey = getPublicKey(rsaToken.getInstanceId(), rsaToken.getServiceId());
			boolean verify = RSAUtils.verify(publicKey, sign, content);
			if (verify) {
				long generateTime = rsaToken.getGenerateTime();
				Date expiredDate = new Date(generateTime + RSAAuthenticationToken.TOKEN_ACTIVE_TIME + 15 * 60 * 1000);
				Date now = new Date();
				if (now.before(expiredDate)) {
					return true;
				}
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {

			return false;
		}
		return false;
	}

	private String getPublicKey(String instanceId, String serviceId) {
		Optional<MicroserviceInstance> instances = Optional
				.ofNullable(MicroserviceInstanceCache.getOrCreate(serviceId, instanceId));
		if (instances.isPresent()) {
			return instances.map(MicroserviceInstance::getProperties)
					.map(properties -> properties.get(Const.INSTANCE_PUBKEY_PRO)).get();
		} else {
			logger.error("not instance found {}-{},maybe attack", instanceId, serviceId);
			return "";
		}
	}

}
