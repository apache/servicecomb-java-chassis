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
package org.apache.servicecomb.match.propertirs;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.Configuration;
import org.apache.servicecomb.config.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

public class SerializeCache<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SerializeCache.class);

  private Map<String, String> md5Map = new HashMap<>();

  private Map<String, T> cacheMap = new HashMap<>();

  private Configuration configuration = ConfigUtil.createLocalConfig();

  public Map<String, T> get(String prefix, Class<T> entityClass) {
    Map<String, String> t = new HashMap<>();
    Iterator<String> keys = configuration.getKeys();
    while (keys.hasNext()) {
      String key = keys.next();
      if (key.startsWith(prefix)) {
        t.put(key.substring(prefix.length()), configuration.getString(key));
      }
    }
    if (CollectionUtils.isEmpty(t)) {
      return Collections.emptyMap();
    }
    for (Entry<String, String> entry : t.entrySet()) {
      String key = entry.getKey();
      if (!check(key, entry.getValue())) {
        continue;
      }
      Yaml yaml = new Yaml();
      try {
        T marker = yaml.loadAs(entry.getValue(), entityClass);
        cacheMap.put(key, marker);
      } catch (YAMLException e) {
        LOGGER.error("governance config yaml is illegal : {}", e.getMessage());
        return Collections.emptyMap();
      }
    }
    Map<String, T> resultMap = new HashMap<>();
    for (Entry<String, T> entry : cacheMap.entrySet()) {
      try {
        if (entry.getValue().getClass().isInstance(entityClass.newInstance())) {
          resultMap.put(entry.getKey(), entry.getValue());
        }
      } catch (InstantiationException | IllegalAccessException e) {
        LOGGER.error("internal error.", e);
      }
    }
    return resultMap;
  }

  private boolean check(String key, String value) {
    String md5Str = encrypt(value);
    if (md5Map.containsKey(value) && md5Map.get(value).equals(md5Str)) {
      return false;
    }
    md5Map.put(key, md5Str);
    return true;
  }


  private static String encrypt(String dataStr) {
    MessageDigest messageDigest = null;
    String result = "";
    try {
      messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.update(dataStr.getBytes(StandardCharsets.UTF_8));
      result = new BigInteger(1, messageDigest.digest(dataStr.getBytes(StandardCharsets.UTF_8)))
          .toString(16);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("Failed to generate MD5 . ", e);
    }
    return result;
  }
}
