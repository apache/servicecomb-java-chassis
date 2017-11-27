package io.servicecomb.authentication.provider;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.authentication.RSAAuthenticationToken;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.MicroserviceInstanceCache;

public class RSAProviderTokenManager  {

  private static Logger logger = LoggerFactory.getLogger(RSAProviderTokenManager.class);

  private Set<RSAAuthenticationToken> validatedToken = ConcurrentHashMap.newKeySet(1000);

  public boolean valid(String token) {
    try {
      RSAAuthenticationToken rsaToken = RSAAuthenticationToken.fromStr(token);
      if (null == rsaToken) {
        logger.error("token format is error, perhaps you need to set auth handler at consumer");
        return false;
      }
      if (tokenExprired(rsaToken)) {
        logger.error("token is expired");
        return false;
      }
      if (validatedToken.contains(rsaToken)) {
        logger.info("found vaildate token in vaildate pool");
        return true;
      } else {
        String sign = rsaToken.getSign();
        String content = rsaToken.plainToken();
        String publicKey = getPublicKey(rsaToken.getInstanceId(), rsaToken.getServiceId());
        boolean verify = RSAUtils.verify(publicKey, sign, content);
        if (verify && !tokenExprired(rsaToken)) {
          validatedToken.add(rsaToken);
          return true;
        } else {
          logger.error("token verify error");
          return false;
        }
      }

    } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
      logger.error("verfiy error", e);
      return false;
    }
  }

  private boolean tokenExprired(RSAAuthenticationToken rsaToken) {
    long generateTime = rsaToken.getGenerateTime();
    Date expiredDate = new Date(generateTime + RSAAuthenticationToken.TOKEN_ACTIVE_TIME + 15 * 60 * 1000);
    Date now = new Date();
    if (now.before(expiredDate)) {
      return false;
    }
    return true;
  }

  private String getPublicKey(String instanceId, String serviceId) {
    Optional<MicroserviceInstance> instances = Optional
        .ofNullable(MicroserviceInstanceCache.getOrCreate(serviceId, instanceId));
    if (instances.isPresent()) {
      return instances.map(MicroserviceInstance::getProperties)
          .map(properties -> properties.get(Const.INSTANCE_PUBKEY_PRO))
          .get();
    } else {
      logger.error("not instance found {}-{}, maybe attack", instanceId, serviceId);
      return "";
    }
  }

}
