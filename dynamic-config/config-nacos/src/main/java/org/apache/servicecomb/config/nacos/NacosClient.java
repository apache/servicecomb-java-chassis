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

package org.apache.servicecomb.config.nacos;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.BootStrapProperties;
import org.apache.servicecomb.config.nacos.NacosDynamicPropertiesSource.UpdateHandler;
import org.apache.servicecomb.config.parser.Parser;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

public class NacosClient {
  private final UpdateHandler updateHandler;

  private final NacosConfig nacosConfig;

  private final Environment environment;

  private final Object lock = new Object();

  private Map<String, Object> application = new HashMap<>();

  private Map<String, Object> service = new HashMap<>();

  private Map<String, Object> version = new HashMap<>();

  private Map<String, Object> profile = new HashMap<>();

  private Map<String, Object> custom = new HashMap<>();

  private Map<String, Object> allLast = new HashMap<>();

  public NacosClient(UpdateHandler updateHandler, Environment environment) {
    this.updateHandler = updateHandler;
    this.nacosConfig = new NacosConfig(environment);
    this.environment = environment;
  }

  public void refreshNacosConfig() throws NacosException {
    Properties properties = nacosProperties(environment, nacosConfig);

    ConfigService configService = NacosFactory.createConfigService(properties);
    addApplicationConfig(configService);
    addServiceConfig(configService);
    addVersionConfig(configService);
    addProfileConfig(configService);
    addCustomConfig(configService);

    refreshConfigItems();
  }

  private void addApplicationConfig(ConfigService configService) throws NacosException {
    String content = configService.getConfig(BootStrapProperties.readApplication(environment),
        BootStrapProperties.readApplication(environment), 5000);
    processApplicationConfig(content);
    configService.addListener(BootStrapProperties.readApplication(environment),
        BootStrapProperties.readApplication(environment), new Listener() {
          @Override
          public void receiveConfigInfo(String configInfo) {
            processApplicationConfig(configInfo);
            refreshConfigItems();
          }

          @Override
          public Executor getExecutor() {
            return null;
          }
        });
  }

  private void processApplicationConfig(String content) {
    if (StringUtils.isEmpty(content)) {
      this.application = new HashMap<>();
      return;
    }
    Parser contentParser = Parser.findParser("yaml");
    this.application = contentParser.parse(content, "", false);
  }

  private void addServiceConfig(ConfigService configService) throws NacosException {
    String content = configService.getConfig(BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readApplication(environment),
        5000);
    processServiceConfig(content);
    configService.addListener(BootStrapProperties.readServiceName(environment),
        BootStrapProperties.readApplication(environment), new Listener() {
          @Override
          public void receiveConfigInfo(String configInfo) {
            processServiceConfig(configInfo);
            refreshConfigItems();
          }

          @Override
          public Executor getExecutor() {
            return null;
          }
        });
  }

  private void processServiceConfig(String content) {
    if (StringUtils.isEmpty(content)) {
      this.service = new HashMap<>();
      return;
    }
    Parser contentParser = Parser.findParser("yaml");
    this.service = contentParser.parse(content, "", false);
  }

  private void addVersionConfig(ConfigService configService) throws NacosException {
    String content = configService.getConfig(
        BootStrapProperties.readServiceName(environment) + "-" +
            BootStrapProperties.readServiceVersion(environment),
        BootStrapProperties.readApplication(environment),
        5000);
    processVersionConfig(content);
    configService.addListener(BootStrapProperties.readServiceName(environment) + "-" +
            BootStrapProperties.readServiceVersion(environment),
        BootStrapProperties.readApplication(environment), new Listener() {
          @Override
          public void receiveConfigInfo(String configInfo) {
            processVersionConfig(configInfo);
            refreshConfigItems();
          }

          @Override
          public Executor getExecutor() {
            return null;
          }
        });
  }

  private void processVersionConfig(String content) {
    if (StringUtils.isEmpty(content)) {
      this.version = new HashMap<>();
      return;
    }
    Parser contentParser = Parser.findParser("yaml");
    this.version = contentParser.parse(content, "", false);
  }

  private void addProfileConfig(ConfigService configService) throws NacosException {
    String profile = environment.getProperty("spring.profiles.active");
    if (StringUtils.isEmpty(profile)) {
      return;
    }
    String content = configService.getConfig(BootStrapProperties.readServiceName(environment) + "-" + profile,
        BootStrapProperties.readApplication(environment), 5000);
    processProfileConfig(content);
    configService.addListener(BootStrapProperties.readServiceName(environment) + "-" + profile,
        BootStrapProperties.readApplication(environment), new Listener() {
          @Override
          public void receiveConfigInfo(String configInfo) {
            processProfileConfig(configInfo);
            refreshConfigItems();
          }

          @Override
          public Executor getExecutor() {
            return null;
          }
        });
  }

  private void processProfileConfig(String content) {
    if (StringUtils.isEmpty(content)) {
      this.profile = new HashMap<>();
      return;
    }
    Parser contentParser = Parser.findParser("yaml");
    this.profile = contentParser.parse(content, "", false);
  }

  private void addCustomConfig(ConfigService configService) throws NacosException {
    if (StringUtils.isEmpty(nacosConfig.getDataId()) || StringUtils.isEmpty(nacosConfig.getGroup())) {
      return;
    }
    String content = configService.getConfig(nacosConfig.getDataId(),
        nacosConfig.getGroup(), 5000);
    processCustomConfig(content);
    configService.addListener(nacosConfig.getDataId(),
        nacosConfig.getGroup(), new Listener() {
          @Override
          public void receiveConfigInfo(String configInfo) {
            processCustomConfig(configInfo);
            refreshConfigItems();
          }

          @Override
          public Executor getExecutor() {
            return null;
          }
        });
  }

  private void processCustomConfig(String content) {
    if (StringUtils.isEmpty(content)) {
      this.custom = new HashMap<>();
      return;
    }
    Parser contentParser = Parser.findParser(nacosConfig.getContentType());
    String keyPrefix = nacosConfig.getGroup() + "." +
        nacosConfig.getDataId();
    this.custom = contentParser.parse(content, keyPrefix, nacosConfig.getAddPrefix());
  }

  private void refreshConfigItems() {
    synchronized (lock) {
      Map<String, Object> all = new HashMap<>();
      all.putAll(application);
      all.putAll(service);
      all.putAll(version);
      all.putAll(profile);
      all.putAll(custom);
      updateHandler.handle(all, allLast);
      this.allLast = all;
    }
  }

  private static Properties nacosProperties(Environment environment, NacosConfig nacosConfig) {
    Properties properties = new Properties();
    properties.put(NacosConfig.PROP_NAMESPACE, BootStrapProperties.readServiceEnvironment(environment));
    properties.put(NacosConfig.PROP_ADDRESS, nacosConfig.getServerAddr());
    if (nacosConfig.getUsername() != null) {
      properties.put(NacosConfig.PROP_USERNAME, nacosConfig.getUsername());
    }
    if (nacosConfig.getPassword() != null) {
      properties.put(NacosConfig.PROP_PASSWORD, nacosConfig.getPassword());
    }
    if (nacosConfig.getAccessKey() != null) {
      properties.put(NacosConfig.PROP_ACCESS_KEY, nacosConfig.getAccessKey());
    }
    if (nacosConfig.getSecretKey() != null) {
      properties.put(NacosConfig.PROP_SECRET_KEY, nacosConfig.getSecretKey());
    }
    return properties;
  }
}
