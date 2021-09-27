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

import java.util.HashMap;
import java.util.Map;

public class ParseRequest {
  public String msgType = "";

  public String strMsg;

  public String strID = "";

  public Map<String, Object> flags = new HashMap<>();

  public String getMsgType() {
    return msgType;
  }

  public void setMsgType(String msgType) {
    this.msgType = msgType;
  }

  public String getStrMsg() {
    return strMsg;
  }

  public void setStrMsg(String strMsg) {
    this.strMsg = strMsg;
  }

  public String getStrID() {
    return strID;
  }

  public void setStrID(String strID) {
    this.strID = strID;
  }

  public Map<String, Object> getFlags() {
    return flags;
  }

  public void setFlags(Map<String, Object> flags) {
    this.flags = flags;
  }
}
