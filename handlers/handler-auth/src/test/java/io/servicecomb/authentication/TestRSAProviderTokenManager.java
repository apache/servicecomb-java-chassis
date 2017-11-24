package io.servicecomb.authentication;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.servicecomb.authentication.consumer.RSACoumserTokenManager;
import io.servicecomb.authentication.provider.RSAProviderTokenManager;
import io.servicecomb.foundation.common.utils.RSAUtils;
import io.servicecomb.foundation.token.RSAKeypair4Auth;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.Const;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.cache.MicroserviceInstanceCache;
@RunWith(PowerMockRunner.class)
@PrepareForTest({MicroserviceInstanceCache.class,RegistryUtils.class})
public class TestRSAProviderTokenManager {
	
	
	@Test
	public void testTokenExpried()
	{
		String tokenStr = "e8a04b54cf2711e7b701286ed488fc20@c8636e5acf1f11e7b701286ed488fc20@1511315597475@9t0tp8ce80SUM5ts6iRGjFJMvCdQ7uvhpyh0RM7smKm3p4wYOrojr4oT1Pnwx7xwgcgEFbQdwPJxIMfivpQ1rHGqiLp67cjACvJ3Ke39pmeAVhybsLADfid6oSjscFaJ@WBYouF6hXYrXzBA31HC3VX8Bw9PNgJUtVqOPAaeW9ye3q/D7WWb0M+XMouBIWxWY6v9Un1dGu5Rkjlx6gZbnlHkb2VO8qFR3Y6lppooWCirzpvEBRjlJQu8LPBur0BCfYGq8XYrEZA2NU6sg2zXieqCSiX6BnMnBHNn4cR9iZpk=";
		RSAProviderTokenManager tokenManager = new RSAProviderTokenManager();
		PowerMockito.mockStatic(MicroserviceInstanceCache.class);
		MicroserviceInstance microserviceInstance = new MicroserviceInstance();
		Map<String, String> properties = new HashMap<String, String>();
		microserviceInstance.setProperties(properties);
		properties.put(Const.INSTANCE_PUBKEY_PRO, "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCxKl5TNUTec7fL2degQcCk6vKf3c0wsfNK5V6elKzjWxm0MwbRj/UeR20VSnicBmVIOWrBS9LiERPPvjmmWUOSS2vxwr5XfhBhZ07gCAUNxBOTzgMo5nE45DhhZu5Jzt5qSV6o10Kq7+fCCBlDZ1UoWxZceHkUt5AxcrhEDulFjQIDAQAB");
		PowerMockito.when(MicroserviceInstanceCache.getOrCreate("c8636e5acf1f11e7b701286ed488fc20", "e8a04b54cf2711e7b701286ed488fc20")).thenReturn(microserviceInstance);
		Assert.assertFalse(tokenManager.valid(tokenStr));
	}
	
	@Test 
	public void testTokenFromVaidatePool()
	{
         String[] keypairs = RSAUtils.getEncodedKeyPair();
	     String privateKey = keypairs[0];
	     String publicKey = keypairs[1];
	     RSAKeypair4Auth.INSTANCE.setPrivateKey(privateKey);
	     RSAKeypair4Auth.INSTANCE.setPublicKey(publicKey);
	     String serviceId = "c8636e5acf1f11e7b701286ed488fc20";
	     String instanceId=  "e8a04b54cf2711e7b701286ed488fc20";
	     RSACoumserTokenManager rsaCoumserTokenManager = new RSACoumserTokenManager();
	     MicroserviceInstance microserviceInstance = new MicroserviceInstance();
	     microserviceInstance.setInstanceId(instanceId);
	     Map<String, String> properties = new HashMap<String, String>();
		 microserviceInstance.setProperties(properties);
		 properties.put(Const.INSTANCE_PUBKEY_PRO, publicKey);
	     Microservice microservice = new Microservice();
	     microservice.setServiceId(serviceId);
	     PowerMockito.mockStatic(RegistryUtils.class);
	     PowerMockito.when(RegistryUtils.getMicroservice()).thenReturn(microservice);
	     PowerMockito.when(RegistryUtils.getMicroserviceInstance()).thenReturn(microserviceInstance);
	     
	     //Test Consumer first create token
	     String token = rsaCoumserTokenManager.getToken(); 
	     Assert.assertNotNull(token);
	     // use cache token
	     Assert.assertEquals(token, rsaCoumserTokenManager.getToken());
	     
	     PowerMockito.mockStatic(MicroserviceInstanceCache.class);
	     PowerMockito.when(MicroserviceInstanceCache.getOrCreate(serviceId, instanceId)).thenReturn(microserviceInstance);
	     RSAProviderTokenManager rsaProviderTokenManager = new RSAProviderTokenManager();
	     //first validate need to verify use RSA
	     Assert.assertTrue(rsaProviderTokenManager.valid(token));
	     // second validate use validated pool
	     PowerMockito.when(MicroserviceInstanceCache.getOrCreate(serviceId, instanceId)).thenReturn(null);
	     Assert.assertTrue(rsaProviderTokenManager.valid(token));
	}

}
