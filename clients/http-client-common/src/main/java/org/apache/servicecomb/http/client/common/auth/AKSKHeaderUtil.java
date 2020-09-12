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

package org.apache.servicecomb.http.client.common.auth;

import java.util.Map;

import org.apache.servicecomb.http.client.common.HttpConfiguration.AKSKProperties;
import org.apache.servicecomb.http.client.common.HttpRequest;

public class AKSKHeaderUtil {
  public static final String X_SERVICE_AK = "X-Service-AK";

  public static final String X_SERVICE_SHAAKSK = "X-Service-ShaAKSK";

  public static final String X_SERVICE_PROJECT = "X-Service-Project";

  public static void addAKSKHeader(HttpRequest httpRequest,
      AKSKProperties serviceCombAkSkProperties) {
    if (serviceCombAkSkProperties.isEnabled()) {
      httpRequest.addHeader(X_SERVICE_AK, serviceCombAkSkProperties.getAccessKey());
      httpRequest.addHeader(X_SERVICE_SHAAKSK, serviceCombAkSkProperties.getSecretKey());
      httpRequest.addHeader(X_SERVICE_PROJECT, serviceCombAkSkProperties.getProject());
      return;
    }

    Map<String, String> headerMap = AKSKHeaderExtensionUtil.genAuthHeaders();
    if (!headerMap.isEmpty()) {
      httpRequest.addHeader(X_SERVICE_AK, headerMap.get(X_SERVICE_AK));
      httpRequest.addHeader(X_SERVICE_SHAAKSK, headerMap.get(X_SERVICE_SHAAKSK));
      httpRequest.addHeader(X_SERVICE_PROJECT, headerMap.get(X_SERVICE_PROJECT));
    }
  }
}
