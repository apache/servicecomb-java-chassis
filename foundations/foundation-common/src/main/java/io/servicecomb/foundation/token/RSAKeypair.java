package io.servicecomb.foundation.token;


public class RSAKeypair {

	private RSAKeypair(){};
	
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
	
	public static RSAKeypair INSTANCE = new RSAKeypair();
}
