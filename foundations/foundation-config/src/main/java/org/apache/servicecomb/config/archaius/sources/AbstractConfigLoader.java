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

package org.apache.servicecomb.config.archaius.sources;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.JvmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

public abstract class AbstractConfigLoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfigLoader.class);

  private static final String ORDER_KEY = "servicecomb-config-order";

  protected final List<ConfigModel> configModels = new ArrayList<>();

  public List<ConfigModel> getConfigModels() {
    return configModels;
  }

  public void load(String resourceName) throws IOException {
    loadFromClassPath(resourceName);
  }

  protected void loadFromClassPath(String resourceName) throws IOException {
    List<URL> urlList = findURLFromClassPath(resourceName);
    for (URL url : urlList) {
      ConfigModel configModel = load(url);
      configModels.add(configModel);
    }
  }

  public ConfigModel load(URL url) throws IOException {
    Map<String, Object> config = loadData(url);
    // load a empty or all commented yaml, will get a null map
    // this is not a error
    if (config == null) {
      config = new LinkedHashMap<>();
    }

    ConfigModel configModel = new ConfigModel();
    configModel.setUrl(url);
    configModel.setConfig(config);

    Object objOrder = config.get(ORDER_KEY);
    if (objOrder == null) {
      // compatible check
      objOrder = config.get("cse-config-order");
      if (objOrder != null) {
        LOGGER.error("cse-config-order will not be supported in future, please change it to servicecomb-config-order");
      }
    }

    if (objOrder != null) {
      if (Integer.class.isInstance(objOrder)) {
        configModel.setOrder((int) objOrder);
      } else {
        configModel.setOrder(Integer.parseInt(String.valueOf(objOrder)));
      }
    }
    return configModel;
  }

  protected abstract Map<String, Object> loadData(URL url) throws IOException;

  protected List<URL> findURLFromClassPath(String resourceName) throws IOException {
    List<URL> urlList = new ArrayList<>();

    ClassLoader loader = JvmUtils.findClassLoader();
    Enumeration<URL> urls = loader.getResources(resourceName);
    while (urls.hasMoreElements()) {
      urlList.add(urls.nextElement());
    }

    return urlList;
  }

  private class ConfigModelWrapper {
    ConfigModel model;

    int addOrder;
  }

  // sort rule:
  // 1.files in jar
  // 2.smaller order
  // 3.add to list earlier
  protected void sort() {
    List<ConfigModelWrapper> list = new ArrayList<>(configModels.size());
    for (int idx = 0; idx < configModels.size(); idx++) {
      ConfigModelWrapper wrapper = new ConfigModelWrapper();
      wrapper.model = configModels.get(idx);
      wrapper.addOrder = idx;
      list.add(wrapper);
    }

    list.sort(this::doSort);

    for (int idx = 0; idx < configModels.size(); idx++) {
      configModels.set(idx, list.get(idx).model);
    }
  }

  private int doSort(ConfigModelWrapper w1, ConfigModelWrapper w2) {
    ConfigModel m1 = w1.model;
    ConfigModel m2 = w2.model;
    boolean isM1Jar = ResourceUtils.isJarURL(m1.getUrl());
    boolean isM2Jar = ResourceUtils.isJarURL(m2.getUrl());
    if (isM1Jar != isM2Jar) {
      if (isM1Jar) {
        return -1;
      }

      return 1;
    }
    // min order load first
    int result = Integer.compare(m1.getOrder(), m2.getOrder());
    if (result != 0) {
      return result;
    }

    return doFinalSort(w1, w2);
  }

  private int doFinalSort(ConfigModelWrapper w1, ConfigModelWrapper w2) {
    return Integer.compare(w1.addOrder, w2.addOrder);
  }
}
