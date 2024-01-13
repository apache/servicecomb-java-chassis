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

import com.ctrip.framework.foundation.internals.Utils;
import com.ctrip.framework.foundation.spi.provider.Provider;
import com.ctrip.framework.foundation.spi.provider.ServerProvider;
import org.apache.servicecomb.config.client.ApolloConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class CSEServerProvider implements ServerProvider {

  private static final Logger logger = LoggerFactory.getLogger(CSEServerProvider.class);

  private static final ApolloConfig APOLLO_CONFIG = ApolloConfig.INSTANCE;
  private String m_env;
  private String m_dc;

  @Override
  public void initialize() {
    initialize(null);
  }

  @Override
  public void initialize(InputStream in) {
    try {
      initEnvType();
      initDataCenter();
    } catch (Throwable ex) {
      logger.error("Initialize DefaultServerProvider failed.", ex);
    }
  }

  @Override
  public String getDataCenter() {
    return m_dc;
  }

  @Override
  public boolean isDataCenterSet() {
    return m_dc != null;
  }

  @Override
  public String getEnvType() {
    return m_env;
  }

  @Override
  public boolean isEnvTypeSet() {
    return m_env != null;
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    if ("env".equalsIgnoreCase(name)) {
      String val = getEnvType();
      return val == null ? defaultValue : val;
    }
    if ("dc".equalsIgnoreCase(name)) {
      String val = getDataCenter();
      return val == null ? defaultValue : val;
    }
    String key = APOLLO_CONFIG.getConfigKeyMap(name);
    return APOLLO_CONFIG.getProperty(key!=null?key:name, defaultValue);
  }

  @Override
  public Class<? extends Provider> getType() {
    return ServerProvider.class;
  }

  private void initEnvType() {
    m_env = APOLLO_CONFIG.getEnv();
    if (!Utils.isBlank(m_env)) {
      m_env = m_env.trim();
      return;
    }
    m_env = null;
    logger.warn(ApolloConfig.SERVER_ENV+" not available. It is set to null");
  }

  private void initDataCenter() {
    m_dc = APOLLO_CONFIG.getServerClusters();
    if (!Utils.isBlank(m_dc)) {
      m_dc = m_dc.trim();
      return;
    }
    m_dc = null;
    logger.warn(ApolloConfig.SERVER_CLUSTERS+" not available. It is set to null");
  }

  @Override
  public String toString() {
    return "CSEServerProvider{" +
            "m_env='" + m_env + '\'' +
            ", m_dc='" + m_dc + '\'' +
            '}';
  }
}
