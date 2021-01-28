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

package org.apache.servicecomb.huaweicloud.dashboard.monitor;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.auth.SignRequest;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(SignUtil.class);

  private static List<AuthHeaderProvider> authHeaderProviders = SPIServiceUtils.
      getSortedService(AuthHeaderProvider.class);

  public static SignRequest createSignRequest(String method, String endpoint, Map<String, String>
      headers, InputStream content) {
    SignRequest signReq = new SignRequest();
    try {
      signReq.setEndpoint(new URI(endpoint));
    } catch (URISyntaxException e) {
      LOGGER.warn("set uri failed, uri is {}, message: {}", endpoint, e.getMessage());
    }

    Map<String, String[]> queryParams = new HashMap<>();
    if (endpoint.contains("?")) {
      String parameters = endpoint.substring(endpoint.indexOf("?") + 1);
      if (!StringUtils.isEmpty(parameters)) {
        String[] parameterArray = parameters.split("&");
        for (String p : parameterArray) {
          String key = p.split("=")[0];
          String value = p.split("=")[1];
          if (!queryParams.containsKey(key)) {
            queryParams.put(key, new String[]{value});
          } else {
            List<String> vals = new ArrayList<>(Arrays.asList(queryParams.get(key)));
            vals.add(value);
            queryParams.put(key, vals.toArray(new String[vals.size()]));
          }
        }
      }
    }

    signReq.setQueryParams(queryParams);
    signReq.setHeaders(headers);
    signReq.setHttpMethod(method);
    signReq.setContent(content);
    return signReq;
  }


  public static List<AuthHeaderProvider> getAuthHeaderProviders() {
    return authHeaderProviders;
  }
}
