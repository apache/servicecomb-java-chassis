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

package org.apache.servicecomb.samples.bmi;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BMIViewObject {

  private double result;

  private String instanceId;

  private String callTime;

  public BMIViewObject(double result, String instanceId, Date now) {
    this.result = result;
    this.instanceId = instanceId;
    this.callTime = new SimpleDateFormat("HH:mm:ss").format(now);
  }

  public double getResult() {
    return result;
  }

  public void setResult(double result) {
    this.result = result;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getCallTime() {
    return callTime;
  }

  public void setCallTime(String callTime) {
    this.callTime = callTime;
  }
}
