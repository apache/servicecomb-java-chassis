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

package org.apache.servicecomb.http.client.utils;

import org.apache.http.HttpStatus;
import org.apache.servicecomb.http.client.common.AbstractAddressManager;
import org.apache.servicecomb.http.client.common.HttpRequest;
import org.apache.servicecomb.http.client.common.HttpResponse;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class ServiceCombServiceAvailableUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServiceAvailableUtils.class);

  public static void checkAddressAvailable(AbstractAddressManager addressManager, String address,
      HttpTransport httpTransport, String path) {
    String formatUrl = addressManager.formatUrl(path, true, address);
    HttpRequest httpRequest = new HttpRequest(formatUrl, null, null, HttpRequest.GET);
    try {
      HttpResponse response = httpTransport.doRequest(httpRequest);
      if (response.getStatusCode() == HttpStatus.SC_OK) {
        addressManager.recordSuccessState(address);
        return;
      }

      // old server does not provide the check api, using TCP checks whether the server is ready.
      if (response.getStatusCode() == HttpStatus.SC_NOT_FOUND && telnetCheckAddress(address)) {
        LOGGER.warn("[{}] path does not provide, tcp check address ready!", path);
        addressManager.recordSuccessState(address);
      }
    } catch (IOException e) {
      LOGGER.error("check isolation address [{}] available error!", address);
    }
  }

  private static boolean telnetCheckAddress(String address) {
    URI ipPort = parseIpPortFromURI(address);
    if (ipPort == null) {
      return false;
    }
    try (Socket s = new Socket()) {
      s.connect(new InetSocketAddress(ipPort.getHost(), ipPort.getPort()), 3000);
      return true;
    } catch (IOException e) {
      LOGGER.warn("ping endpoint {} failed, It will be quarantined again.", address);
    }
    return false;
  }

  private static URI parseIpPortFromURI(String address) {
    try {
      return new URI(address);
    } catch (URISyntaxException e) {
      LOGGER.error("build uri error with address [{}].", address);
      return null;
    }
  }
}
