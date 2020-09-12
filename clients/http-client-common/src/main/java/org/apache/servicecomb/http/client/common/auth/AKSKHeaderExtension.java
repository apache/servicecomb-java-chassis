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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class AKSKHeaderExtension {

  private static final Logger LOGGER = LoggerFactory.getLogger(AKSKHeaderExtension.class);

  protected static final String DEFAULT_SECRET_AUTH_PATH = "/opt/CSE/etc/auth";

  protected static final String DEFAULT_SECRET_AUTH_NAME = ".dockerconfigjson";

  private static final int EXPECTED_ARR_LENGTH = 2;

  private boolean runOverHWC = !StringUtils.isEmpty(System.getenv("KUBERNETES_SERVICE_HOST"));

  private Map<String, String> defaultAuthHeaders = Collections.emptyMap();

  private boolean loaded = false;

  protected Map<String, String> getHeaders() {
    if (!runOverHWC) {
      return defaultAuthHeaders;
    }
    if (!loaded) {
      synchronized (this) {
        if (!loaded) {
          createAuthHeaders();
        }
      }
    }
    return defaultAuthHeaders;
  }

  public abstract void createAuthHeaders();

  protected void decode(JsonNode authNode) throws IOException {
    if (authNode == null) {
      return;
    }
    Map<String, String> authHeaders = new HashMap<String, String>();
    String authStr = authNode.asText();
    String authBase64Decode = new String(Base64.decodeBase64(authStr), "UTF-8");
    String[] auths = authBase64Decode.split("@");
    String[] akAndShaAkSk = auths[1].split(":");
    if (auths.length != EXPECTED_ARR_LENGTH || akAndShaAkSk.length != EXPECTED_ARR_LENGTH) {
      LOGGER.error("get docker config failed. The data is not valid cause of unexpected format");
      return;
    }
    String project = auths[0];
    String ak = akAndShaAkSk[0];
    String shaAkSk = akAndShaAkSk[1];
    authHeaders.put("X-Service-AK", ak);
    authHeaders.put("X-Service-ShaAKSK", shaAkSk);
    authHeaders.put("X-Service-Project", project);
    defaultAuthHeaders = authHeaders;
    loaded = true;
  }
}
