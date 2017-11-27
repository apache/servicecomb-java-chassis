package io.servicecomb.authentication;


/**
 * token 组成部分：
 * token: instanceId@@generateTime@randomCode@sign(instanceId@@generateTime@randomCode)
 *
 */
public class RSAAuthenticationToken {

  public final static long TOKEN_ACTIVE_TIME = 24 * 60 * 60 * 1000;

  private String instanceId;

  private String serviceId;

  private long generateTime;

  private String randomCode;

  private String sign;

  public RSAAuthenticationToken(String instanceId, String serviceId, long generateTime,
      String randomCode, String sign) {
    this.instanceId = instanceId;
    this.generateTime = generateTime;
    this.randomCode = randomCode;
    this.serviceId = serviceId;
    this.sign = sign;
  }

  public String plainToken() {
    return String.format("%s@%s@%s@%s", this.instanceId, this.serviceId, this.generateTime, this.randomCode);
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


  public String format() {
    return String.format("%s@%s@%s@%s@%s",
        instanceId,
        serviceId,
        generateTime,
        randomCode,
        sign);
  }

  public static RSAAuthenticationToken fromStr(String token) {
    String[] tokenArr = token.split("@");
    if (tokenArr.length != 5) {
      return null;
    }
    return new RSAAuthenticationToken(tokenArr[0], tokenArr[1],
        Long.valueOf(tokenArr[2]), tokenArr[3], tokenArr[4]);
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  @Override
  public boolean equals(Object obj) {
    if (null == obj || !(obj instanceof RSAAuthenticationToken)) {
      return false;
    }
    RSAAuthenticationToken token = (RSAAuthenticationToken) obj;
    if (!token.plainToken().equals(this.plainToken())) {
      return false;
    }
    if (!token.getSign().equals(this.sign)) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    return this.plainToken().hashCode() + this.sign.hashCode();
  }


}
