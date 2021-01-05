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
package org.apache.servicecomb.governance.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.servicecomb.governance.entity.Configurable;
import org.apache.servicecomb.governance.event.ConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.eventbus.Subscribe;

public abstract class GovernanceProperties<T extends Configurable> implements InitializingBean {
  private static final Logger LOGGER = LoggerFactory.getLogger(GovernanceProperties.class);

  private final Representer representer = new Representer();

  private final String configKey;

  @Autowired
  protected Environment environment;

  protected Map<String, T> parsedEntity;

  protected Class<T> entityClass;

  protected GovernanceProperties(String key) {
    configKey = key;
    representer.getPropertyUtils().setSkipMissingProperties(true);
    EventManager.register(this);
    entityClass = getEntityClass();
  }

  @Override
  public void afterPropertiesSet() {
    parsedEntity = parseEntity(readPropertiesFromPrefix());
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    for (String key : event.getChangedConfigurations()) {
      if (key.startsWith(configKey + ".")) {
        // 删除的情况， 从配置文件读取配置。 需要保证 environment 已经刷新配置值。
        T entityItem = parseEntityItem(key, environment.getProperty(key));
        String mapKey = key.substring((configKey + ".").length());
        if (entityItem == null) {
          parsedEntity.remove(mapKey);
        } else {
          parsedEntity.put(mapKey, entityItem);
        }
      }
    }
  }

  private Map<String, String> readPropertiesFromPrefix() {
    Set<String> allKeys = getAllKeys(environment);
    Map<String, String> result = new HashMap<>();
    allKeys.forEach(key -> {
      if (key.startsWith(configKey + ".")) {
        result.put(key.substring(configKey.length() + 1), environment.getProperty(key));
      }
    });
    return result;
  }

  private Set<String> getAllKeys(Environment environment) {
    Set<String> allKeys = new HashSet<>();

    if (!(environment instanceof ConfigurableEnvironment)) {
      LOGGER.warn("None ConfigurableEnvironment is ignored in {}", this.getClass().getName());
      return allKeys;
    }

    ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;

    for (PropertySource<?> propertySource : configurableEnvironment.getPropertySources()) {
      getProperties(propertySource, allKeys);
    }
    return allKeys;
  }

  private void getProperties(PropertySource<?> propertySource,
      Set<String> allKeys) {
    if (propertySource instanceof CompositePropertySource) {
      // recursively get EnumerablePropertySource
      CompositePropertySource compositePropertySource = (CompositePropertySource) propertySource;
      compositePropertySource.getPropertySources().forEach(ps -> getProperties(ps, allKeys));
      return;
    }
    if (propertySource instanceof EnumerablePropertySource) {
      EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
      Collections.addAll(allKeys, enumerablePropertySource.getPropertyNames());
      return;
    }

    LOGGER.warn("None EnumerablePropertySource ignored in {}, propertySourceName = [{}]", this.getClass().getName(),
        propertySource.getName());
  }

  public Map<String, T> getParsedEntity() {
    return this.parsedEntity;
  }

  protected Map<String, T> parseEntity(Map<String, String> yamlEntity) {
    if (CollectionUtils.isEmpty(yamlEntity)) {
      return Collections.emptyMap();
    }

    Map<String, T> resultMap = new HashMap<>();
    for (Entry<String, String> entry : yamlEntity.entrySet()) {
      T marker = parseEntityItem(entry.getKey(), entry.getValue());
      if (marker != null) {
        resultMap.put(entry.getKey(), marker);
      }
    }
    return resultMap;
  }

  protected abstract Class<T> getEntityClass();

  protected abstract void setName(T value, String key);

  protected T parseEntityItem(String key, String value) {
    if (StringUtils.isEmpty(value)) {
      return null;
    }

    try {
      Yaml entityParser = new Yaml(new Constructor(new TypeDescription(entityClass, entityClass)), representer);
      T result = entityParser.loadAs(value, entityClass);
      setName(result, key);

      if (!result.isValid()) {
        LOGGER.warn("Entity configuration is not valid and ignored. Key [{}], value [{}]", key, value);
        return null;
      }
      return result;
    } catch (YAMLException e) {
      LOGGER.error("governance config yaml is illegal : {}", e.getMessage());
    }
    return null;
  }
}
