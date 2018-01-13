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

package org.apache.servicecomb.demo.pojo.server;

import org.apache.servicecomb.demo.smartcare.Application;
import org.apache.servicecomb.demo.smartcare.Response;
import org.apache.servicecomb.demo.smartcare.SmartCare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartCareImpl implements SmartCare {
  private static final Logger LOG = LoggerFactory.getLogger(SmartCareImpl.class);

  @Override
  public Response addApplication(Application application) {
    // TODO: add application
    LOG.info(application.toString());

    Response resp = new Response();
    resp.setResultCode(0);
    resp.setResultMessage("add application " + application.getName() + " success");
    return resp;
  }

  @Override
  public Response delApplication(String appName) {
    // TODO: delete application
    LOG.info(appName);

    try {
      System.out.println(5 / 0);
    } catch (Exception e) {
      Response resp = new Response();
      resp.setResultCode(1);
      resp.setResultMessage("delete application " + appName + " failed");
      return resp;
    }

    return null;
  }
}
