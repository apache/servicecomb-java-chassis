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
package com.huaweicloud.governance.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

public abstract class GovProperties<T> implements InitializingBean {
  private static final Logger LOGGER = LoggerFactory.getLogger(GovProperties.class);

  private Yaml safeParser = new Yaml(new SafeConstructor());

  private Representer representer = new Representer();

  private final String configKey;

  @Autowired
  protected Environment environment;

  protected Map<String, T> parsedEntity;

  protected GovProperties(String key) {
    configKey = key;
    representer.getPropertyUtils().setSkipMissingProperties(true);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    String data = environment.getProperty(configKey);
    parsedEntity = covert(loadData(data));
  }

  public Map<String, T> getParsedEntity() {
    return this.parsedEntity;
  }

  protected abstract Map<String, T> covert(Map<String, String> properties);

  protected Map<String, String> loadData(String data) {
    if (StringUtils.isEmpty(data)) {
      return null;
    }
    return safeParser.load(data);
  }

  protected Map<String, T> parseEntity(Map<String, String> t, Class<T> entityClass) {
    if (CollectionUtils.isEmpty(t)) {
      return Collections.emptyMap();
    }

    Yaml entityParser = new Yaml(new Constructor(new TypeDescription(entityClass, entityClass)), representer);

    Map<String, T> resultMap = new HashMap<>();
    String classKey = entityClass.getName();
    for (Entry<String, String> entry : t.entrySet()) {
      try {
        T marker = entityParser.loadAs(entry.getValue(), entityClass);
        resultMap.put(classKey, marker);
      } catch (YAMLException e) {
        LOGGER.error("governance config yaml is illegal : {}", e.getMessage());
      }
    }
    return resultMap;
  }
}
