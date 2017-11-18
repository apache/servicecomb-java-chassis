package io.servicecomb.foundation.common.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.junit.Assert;
import org.junit.Test;

public class TestRSAUtil {

	@Test
	public void testSignVerify() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException
	{
		String []keypair = RSAUtils.getEncodedKeyPair();
		
		String privateKey = keypair[0];
		String pubKey = keypair[1];
		
		Assert.assertNotNull(privateKey);
		Assert.assertNotNull(pubKey);
		String testContent = "instance-id@201711201930@randomstr";
		String signstr = RSAUtils.sign(testContent, privateKey);
		System.err.println(signstr);
		Assert.assertTrue(RSAUtils.verify(pubKey, signstr, testContent));
		
	}
}
