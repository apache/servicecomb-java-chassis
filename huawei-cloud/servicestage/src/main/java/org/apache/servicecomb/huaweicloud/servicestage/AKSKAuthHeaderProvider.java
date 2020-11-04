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

package org.apache.servicecomb.huaweicloud.servicestage;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.auth.Cipher;
import org.apache.servicecomb.foundation.auth.DefaultCipher;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;

public class AKSKAuthHeaderProvider implements AuthHeaderProvider {

  private static final String CONFIG_AKSK_ENABLED = "servicecomb.credentials.akskEnabled";

  private static final String CONFIG_ACCESS_KEY = "servicecomb.credentials.accessKey";

  private static final String CONFIG_SECRET_KEY = "servicecomb.credentials.secretKey";

  private static final String CONFIG_CIPHER = "servicecomb.credentials.akskCustomCipher";

  private static final String CONFIG_PROJECT = "servicecomb.credentials.project";

  private static final String VALUE_DEFAULT_PROJECT = "default";

  private static final String VALUE_DEFAULT_CIPHER = "default";

  private static final String X_SERVICE_AK = "X-Service-AK";

  private static final String X_SERVICE_SHAAKSK = "X-Service-ShaAKSK";

  private static final String X_SERVICE_PROJECT = "X-Service-Project";

  private Map<String, String> headers = new HashMap<>();

  private Configuration configuration = ConfigUtil.createLocalConfig();

  private boolean enabled = configuration.getBoolean(CONFIG_AKSK_ENABLED, true);

  private boolean loaded = false;

  public Map<String, String> authHeaders() {
    if (!enabled) {
      return headers;
    }

    if (!loaded) {
      load();
    }
    return headers;
  }

  private synchronized void load() {
    if (!loaded) {
      headers.put(X_SERVICE_AK, getAccessKey());
      headers.put(X_SERVICE_SHAAKSK, getSecretKey());
      headers.put(X_SERVICE_PROJECT, getProject());
    }
  }

  private String getAccessKey() {
    return configuration.getString(CONFIG_ACCESS_KEY, "");
  }

  private String getCipher() {
    return configuration.getString(CONFIG_CIPHER, VALUE_DEFAULT_CIPHER);
  }

  private String getSecretKey() {
    String secretKey = configuration.getString(CONFIG_SECRET_KEY, "");
    return new String(findCipher().decrypt(secretKey.toCharArray()));
  }

  private String getProject() {
    return configuration.getString(CONFIG_PROJECT, VALUE_DEFAULT_PROJECT);
  }

  private Cipher findCipher() {
    if (DefaultCipher.DEFAULT_CYPHER.equals(getCipher())) {
      return DefaultCipher.getInstance();
    }

    Map<String, Cipher> cipherBeans = BeanUtils.getBeansOfType(Cipher.class);
    return cipherBeans.values().stream().filter(c -> c.name().equals(getCipher())).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("failed to find cipher named " + getCipher()));
  }
}
