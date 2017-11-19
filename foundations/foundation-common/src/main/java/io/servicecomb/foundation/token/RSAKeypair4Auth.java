package io.servicecomb.foundation.token;

/**
 * 进程级别公私钥对
 *
 */
public class RSAKeypair4Auth {

	private RSAKeypair4Auth(){};
	
	private String privateKey;
	
	private String publicKey;

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
	
	public static RSAKeypair4Auth INSTANCE = new RSAKeypair4Auth();
}
