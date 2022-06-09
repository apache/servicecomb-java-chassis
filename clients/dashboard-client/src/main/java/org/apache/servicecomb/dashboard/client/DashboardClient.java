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

package org.apache.servicecomb.dashboard.client;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.dashboard.client.model.MonitorData;
import org.apache.servicecomb.http.client.common.HttpRequest;
import org.apache.servicecomb.http.client.common.HttpResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DashboardClient implements DashboardOperation {
  private static final Logger LOGGER = LoggerFactory.getLogger(DashboardClient.class);

  protected HttpTransport httpTransport;

  private final DashboardAddressManager addressManager;

  public DashboardClient(DashboardAddressManager addressManager, HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
    this.addressManager = addressManager;
  }

  @Override
  public void sendData(String url, MonitorData data) {
    String address = addressManager.address();
    try {
      HttpRequest httpRequest = new HttpRequest(address + url, null, HttpUtils.serialize(data), HttpRequest.POST);
      HttpResponse httpResponse = httpTransport.doRequest(httpRequest);
      if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {
        LOGGER.error("send data to [{}] failed, status code [{}], message [{}]", url, httpResponse.getStatusCode()
            , httpResponse.getContent());
      }
    } catch (Exception e) {
      LOGGER.error("send data to [{}] failed", url, e);
    }
  }
}
