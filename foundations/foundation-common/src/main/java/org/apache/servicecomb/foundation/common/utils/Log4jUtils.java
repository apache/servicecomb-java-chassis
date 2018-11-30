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

package org.apache.servicecomb.foundation.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.servicecomb.foundation.common.config.impl.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public final class Log4jUtils {

  private static final String MERGED_FILE = "merged.log4j.properties";

  public static final String OUTPUT_CONFIG_ENABLED = "log4j.logger.outputConfig.enabled";

  public static final String OUTPUT_CONFIG_ENABLED_TRUE = "true";

  // spring boot的包装中会重复调用init，需要规避一下
  private static boolean inited = false;

  private static final Object LOCK = new Object();

  private Log4jUtils() {
  }

  public static void init() throws Exception {
    init(Arrays.asList("classpath*:config/base/log4j.properties", "classpath*:config/log4j.properties"));
  }

  public static void init(String locationPattern) throws Exception {
    init(Arrays.asList(locationPattern));
  }

  public static void init(List<String> locationPatterns) throws Exception {
    if (inited) {
      return;
    }

    synchronized (LOCK) {
      if (inited) {
        return;
      }

      PropertiesLoader loader = new PropertiesLoader(locationPatterns);
      Properties properties = loader.load();
      if (properties.isEmpty()) {
        throw new Exception("can not find resource " + locationPatterns);
      }

      PropertyConfigurator.configure(properties);
      inited = true;

      if (OUTPUT_CONFIG_ENABLED_TRUE.equals(
          properties.getProperty(OUTPUT_CONFIG_ENABLED, OUTPUT_CONFIG_ENABLED_TRUE))) {
        // If the property file with the highest priority is on a hard disk(not in a jar package)
        // and we have write access, output the merged property file for the purpose of debugging
        outputFile(loader.getFoundResList(), properties);
      }
    }
  }

  private static void outputFile(List<Resource> resList,
      Properties properties) throws IOException {
    //不可以作为class的变量初始化，因为在outputFile前一句log机制才初始化完成的
    //must create org.slf4j.impl.Log4jLoggerAdapter by LoggerExtFactory
    //in order to redefine Log4jLoggerAdapter before other class load Log4jLoggerAdapter
    Logger log = LoggerFactory.getLogger(Log4jUtils.class);

    String content = genFileContext(resList, properties);
    //不打印配置信息,有密钥等敏感信息
    //log.info("Merged log4j:\n{}", content);

    Resource res = resList.get(resList.size() - 1);
    // 不能直接使用res.getFile，因为jar里面的资源，getFile会抛异常
    File file = new File(res.getURL().getPath());
    if (!file.getParentFile().canWrite()) {
      log.error("Can not output {},because can not write to directory of file {}",
          MERGED_FILE,
          res.getURL().getPath());
      return;
    }

    File mergedfile = new File(res.getFile().getParentFile(), MERGED_FILE);
    FileUtils.writeStringToFile(mergedfile, content);
    log.info("Write merged log4j config file to {}", mergedfile.getAbsolutePath());
  }

  private static String genFileContext(List<Resource> resList, Properties properties) throws IOException {
    List<Entry<Object, Object>> entryList = properties.entrySet()
        .stream()
        .sorted(new Comparator<Entry<Object, Object>>() {
          @Override
          public int compare(Entry<Object, Object> o1, Entry<Object, Object> o2) {
            return o1.getKey().toString().compareTo(o2.getKey().toString());
          }
        })
        .collect(Collectors.toList());

    StringBuilder sb = new StringBuilder();
    for (Resource res : resList) {
      sb.append("#").append(res.getURL().getPath()).append("\n");
    }
    for (Entry<Object, Object> entry : entryList) {
      sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
    }
    return sb.toString();
  }
}
