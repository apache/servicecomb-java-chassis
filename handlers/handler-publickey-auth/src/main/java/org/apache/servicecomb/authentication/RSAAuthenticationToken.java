/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicecomb.authentication;


/**
 * token 组成部分：
 * token: instanceId@@generateTime@randomCode@sign(instanceId@@generateTime@randomCode)
 *
 */
public class RSAAuthenticationToken {

  public static final long TOKEN_ACTIVE_TIME = 24 * 60 * 60 * 1000;

  private final String instanceId;

  private final String serviceId;

  private final long generateTime;

  private final String randomCode;

  private final String sign;

  private final String tokenFormat;

  private final String plainToken;

  public RSAAuthenticationToken(String instanceId, String serviceId, long generateTime,
      String randomCode, String sign) {
    this.instanceId = instanceId;
    this.generateTime = generateTime;
    this.randomCode = randomCode;
    this.serviceId = serviceId;
    this.sign = sign;
    this.tokenFormat = String.format("%s@%s@%s@%s@%s",
        instanceId,
        serviceId,
        generateTime,
        randomCode,
        sign);
    this.plainToken = String.format("%s@%s@%s@%s", this.instanceId, this.serviceId, this.generateTime, this.randomCode);
  }

  public String plainToken() {
    return this.plainToken;
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
    return tokenFormat;
  }

  public static RSAAuthenticationToken fromStr(String token) {
    String[] tokenArr = token.split("@");
    if (tokenArr.length != 5) {
      return null;
    }
    return new RSAAuthenticationToken(tokenArr[0], tokenArr[1],
        Long.parseLong(tokenArr[2]), tokenArr[3], tokenArr[4]);
  }

  public String getServiceId() {
    return serviceId;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RSAAuthenticationToken)) {
      return false;
    }
    RSAAuthenticationToken token = (RSAAuthenticationToken) obj;
    if (!token.plainToken().equals(this.plainToken())) {
      return false;
    }
    return token.getSign().equals(this.sign);
  }

  public int hashCode() {
    return this.plainToken().hashCode() + this.sign.hashCode();
  }
}
