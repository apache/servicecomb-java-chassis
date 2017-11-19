package io.servicecomb.foundation.token;

public class RSAAuthenticationToken {
	
	public final static long TOKEN_ACTIVE_TIME = 24 * 60 * 60 *1000;

	private String instanceId;

	private long generateTime;

	private String randomCode;

	private String sign;

	public RSAAuthenticationToken(String instanceId, long generateTime,
			String randomCode, String sign) {
		this.instanceId = instanceId;
		this.generateTime = generateTime;
		this.randomCode = randomCode;
		this.sign = sign;
	}

	
	public String getInstanceId() {
		return instanceId;
	}


	public long getGenerateTime() {
		return generateTime;
	}


	public String getSign() {
		return sign;
	}


	public String fromat() {
		return String.format("%s@%s@%s@%s", instanceId, randomCode,
				generateTime, sign);
	}
	
	public static RSAAuthenticationToken fromStr(String token) {
		String[] tokenArr = token.split("@");
		return new RSAAuthenticationToken(tokenArr[0],
				Long.valueOf(tokenArr[1]), tokenArr[2], tokenArr[3]);
	}
}
