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

package org.apache.servicecomb.demo.mapnull;

import java.util.Map;

public class ParseResponse {
  public String resultCode = "99999999";

  public String resultInfo = "unknown result";

  public String msgType = "";

  public Map<String, String> msgHeader;

  public Map<String, Object> msgBody;

  public String getResultCode() {
    return resultCode;
  }

  public void setResultCode(String resultCode) {
    this.resultCode = resultCode;
  }

  public String getResultInfo() {
    return resultInfo;
  }

  public void setResultInfo(String resultInfo) {
    this.resultInfo = resultInfo;
  }

  public String getMsgType() {
    return msgType;
  }

  public void setMsgType(String msgType) {
    this.msgType = msgType;
  }

  public Map<String, String> getMsgHeader() {
    return msgHeader;
  }

  public void setMsgHeader(Map<String, String> msgHeader) {
    this.msgHeader = msgHeader;
  }

  public Map<String, Object> getMsgBody() {
    return msgBody;
  }

  public void setMsgBody(Map<String, Object> msgBody) {
    this.msgBody = msgBody;
  }
}
