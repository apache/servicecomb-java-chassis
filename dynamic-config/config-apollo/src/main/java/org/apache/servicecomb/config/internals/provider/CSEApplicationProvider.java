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
package org.apache.servicecomb.config.internals.provider;

import com.ctrip.framework.apollo.core.ApolloClientSystemConsts;
import com.ctrip.framework.foundation.internals.Utils;
import com.ctrip.framework.foundation.spi.provider.ApplicationProvider;
import com.ctrip.framework.foundation.spi.provider.Provider;
import org.apache.servicecomb.config.client.ApolloConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class CSEApplicationProvider implements ApplicationProvider {

  private static final Logger logger = LoggerFactory.getLogger(CSEApplicationProvider.class);
  private static final ApolloConfig APOLLO_CONFIG = ApolloConfig.INSTANCE;
  private String m_appId;
  private String m_appLabel;
  private String accessKeySecret;

  @Override
  public void initialize() {
    initialize(null);
  }

  @Override
  public void initialize(InputStream in) {
    try {
      initAppId();
      initAppLabel();
      initAccessKey();
    } catch (Throwable ex) {
      logger.error("Initialize DefaultApplicationProvider failed.", ex);
    }
  }

  @Override
  public String getAppId() {
    return m_appId;
  }

  @Override
  public String getApolloLabel() {
    return m_appLabel;
  }

  @Override
  public String getAccessKeySecret() {
    return accessKeySecret;
  }

  @Override
  public boolean isAppIdSet() {
    return !Utils.isBlank(m_appId);
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    if (ApolloClientSystemConsts.APP_ID.equals(name)) {
      String val = getAppId();
      return val == null ? defaultValue : val;
    }

    if (ApolloClientSystemConsts.APOLLO_ACCESS_KEY_SECRET.equals(name)) {
      String val = getAccessKeySecret();
      return val == null ? defaultValue : val;
    }
    String key = APOLLO_CONFIG.getConfigKeyMap(name);
    return APOLLO_CONFIG.getProperty(key!=null?key:name, defaultValue);
  }

  @Override
  public Class<? extends Provider> getType() {
    return ApplicationProvider.class;
  }

  private void initAppId() {
    m_appId = APOLLO_CONFIG.getServiceName();
    if (!Utils.isBlank(m_appId)) {
      return;
    }
    m_appId = null;
    logger.warn(ApolloConfig.APOLLO_SERVICE_NAME+" is not available. It is set to null");
  }

  private void initAppLabel() {
    m_appLabel = APOLLO_CONFIG.getApolloLabel();
    if (!Utils.isBlank(m_appLabel)) {
      return;
    }
    m_appLabel = null;
    logger.warn(ApolloConfig.APOLLO_CONFIG_LABEL+"not available. It is set to null");
  }

  private void initAccessKey() {
    accessKeySecret = APOLLO_CONFIG.getAccessKeySecret();
    if (!Utils.isBlank(accessKeySecret)) {
      return;
    }
    accessKeySecret = null;
    logger.warn(ApolloConfig.ACCESS_KEY_SECRET+" is not available. It is set to null");
  }

  @Override
  public String toString() {
    return "CSEApplicationProvider{" +
            "m_appId='" + m_appId + '\'' +
            ", m_appLabel='" + m_appLabel + '\'' +
            ", accessKeySecret='" + accessKeySecret + '\'' +
            '}';
  }
}
