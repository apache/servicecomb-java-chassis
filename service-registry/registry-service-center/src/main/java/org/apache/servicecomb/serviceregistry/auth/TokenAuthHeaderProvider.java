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

package org.apache.servicecomb.serviceregistry.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.auth.Cipher;
import org.apache.servicecomb.foundation.auth.DefaultCipher;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.serviceregistry.ServiceRegistry;

import com.netflix.config.DynamicPropertyFactory;

public class TokenAuthHeaderProvider implements AuthHeaderProvider {
  public static final String ACCOUNT_NAME_KEY = "servicecomb.credentials.account.name";

  public static final String PASSWORD_KEY = "servicecomb.credentials.account.password";

  public static final String CIPHER_KEY = "servicecomb.credentials.cipher";

  private String registryName;

  private String accountName;

  private String password;

  private String cipherName;

  public TokenAuthHeaderProvider() {
    this.registryName = ServiceRegistry.DEFAULT_REGISTRY_NAME;
    this.accountName = DynamicPropertyFactory.getInstance()
        .getStringProperty(ACCOUNT_NAME_KEY, null).get();
    this.password = DynamicPropertyFactory.getInstance()
        .getStringProperty(PASSWORD_KEY, null).get();
    this.cipherName = DynamicPropertyFactory.getInstance()
        .getStringProperty(CIPHER_KEY, DefaultCipher.DEFAULT_CYPHER).get();
    if (StringUtils.isNotEmpty(accountName)) {
      TokenCacheManager.getInstance().addTokenCache(registryName, accountName, password, getCipher());
    }
  }

  public TokenAuthHeaderProvider(String registryName, String accountName, String password, String cipherName) {
    this.registryName = registryName;
    this.accountName = accountName;
    this.password = password;
    this.cipherName = cipherName;
    TokenCacheManager.getInstance().addTokenCache(this.registryName, this.accountName, this.password, getCipher());
  }

  @Override
  public Map<String, String> authHeaders() {
    String token = TokenCacheManager.getInstance().getToken(registryName);
    if (StringUtils.isEmpty(token)) {
      return new HashMap<>();
    }

    HashMap<String, String> header = new HashMap<>();
    header.put("Authorization", "Bearer " + token);
    return Collections.unmodifiableMap(header);
  }

  private Cipher getCipher() {
    if (DefaultCipher.DEFAULT_CYPHER.equals(cipherName)) {
      return DefaultCipher.getInstance();
    }

    Map<String, Cipher> cipherBeans = BeanUtils.getBeansOfType(Cipher.class);
    return cipherBeans.values().stream().filter(c -> c.name().equals(cipherName)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("failed to find cipher named " + cipherName));
  }
}
