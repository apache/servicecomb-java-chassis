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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.auth.Cipher;
import org.apache.servicecomb.foundation.auth.DefaultCipher;
import org.apache.servicecomb.foundation.auth.ShaAKSKCipher;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AKSKAuthHeaderProvider implements AuthHeaderProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(AKSKAuthHeaderProvider.class);

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

  private final Configuration configuration;

  private boolean enabled;

  private boolean loaded = false;

  public AKSKAuthHeaderProvider() {
    this(ConfigUtil.createLocalConfig());
  }

  public AKSKAuthHeaderProvider(Configuration configuration) {
    this.configuration = configuration;
    this.enabled = configuration.getBoolean(CONFIG_AKSK_ENABLED, true);
  }

  public Map<String, String> authHeaders() {
    if (!enabled) {
      return headers;
    }

    if (StringUtils.isEmpty(getAccessKey())) {
      LOGGER.warn("ak sk auth enabled but access key is not configured, disable it at runtime. "
              + "Config [{}] to false to disable it implicitly.",
          CONFIG_AKSK_ENABLED);
      enabled = false;
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
    String decodedSecretKey = new String(findCipher().decrypt(secretKey.toCharArray()));

    // ShaAKSKCipher 不解密, 认证的时候不处理；其他算法解密为 plain，需要 encode 为 ShaAKSKCipher 去认证。
    if (ShaAKSKCipher.CIPHER_NAME.equalsIgnoreCase(getCipher())) {
      return decodedSecretKey;
    } else {
      return sha256Encode(decodedSecretKey, getAccessKey());
    }
  }

  private String getProject() {
    String project = configuration.getString(CONFIG_PROJECT, VALUE_DEFAULT_PROJECT);
    if (StringUtils.isEmpty(project)) {
      return project;
    }
    try {
      return URLEncoder.encode(project, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return project;
    }
  }

  private Cipher findCipher() {
    if (DefaultCipher.CIPHER_NAME.equals(getCipher())) {
      return DefaultCipher.getInstance();
    }

    List<Cipher> ciphers = SPIServiceUtils.getOrLoadSortedService(Cipher.class);
    return ciphers.stream().filter(c -> c.name().equals(getCipher())).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("failed to find cipher named " + getCipher()));
  }

  public static String sha256Encode(String key, String data) {
    try {
      Mac sha256HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),
          "HmacSHA256");
      sha256HMAC.init(secretKey);
      return Hex.encodeHexString(sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalArgumentException("Can not encode ak sk. Please check the value is correct.", e);
    }
  }
}
