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

package org.apache.servicecomb.foundation.common.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.servicecomb.foundation.common.config.impl.PropertiesLoader;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

public class PaaSResourceUtils extends org.springframework.util.ResourceUtils {
  public static final String PROPERTIES_SUFFIX = ".properties";

  public static final String XML_SUFFIX = ".xml";

  private static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

  /**
   * 失败，则返回空数组
   */
  public static Resource[] getResources(String locationPattern) {
    try {
      return resourcePatternResolver.getResources(locationPattern);
    } catch (IOException e) {
      return new Resource[0];
    }
  }

  /**
   * 失败，则返回空列表
   */
  public static List<Resource> getResources(String... locationPatterns) {
    List<Resource> ret = new ArrayList<>();
    for (String locationPattern : locationPatterns) {
      Resource[] resArr = getResources(locationPattern);
      ret.addAll(Arrays.asList(resArr));
    }

    return ret;
  }

  /**
   * 文件同名时，jar的优先级比较低
   * jar: xxx.xml
   * jar: xxx.model.xml
   * file:xxx.xml
   * 调用者保证，所有res的后缀都是suffix
   * file文件应该只有一个，因为放在目录中的配置文件，应该是最终的部署定制文件
   * 此时，还分多个，是不合适的
   */
  public static void sortResources(List<Resource> resList, String suffix) {
    resList.sort(new Comparator<Resource>() {
      @Override
      public int compare(Resource o1, Resource o2) {
        try {
          // jar的优先级比较低
          if (isJarURL(o1.getURL()) && isFileURL(o2.getURL())) {
            return -1;
          }

          // 干掉后缀，再排序
          String name1 = o1.getFilename();
          String name2 = o2.getFilename();

          //Resource.getFilename接口会返回null，当路径的文件名不存在时
          //配置文件一定存在，并且Resource有合法的文件名，name1和name2不可能为空，
          //这里做判断是为了处理CodeDEX报的null引用
          if (StringUtils.isEmpty(name1) || StringUtils.isEmpty(name2)) {
            throw new IOException(
                String.format("Resource %s or %s is not a file", o1.getURI(), o2.getURI()));
          }

          name1 = name1.substring(0, name1.length() - suffix.length());
          name2 = name2.substring(0, name2.length() - suffix.length());

          return name1.compareTo(name2);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  public static void sortProperties(List<Resource> resList) {
    sortResources(resList, PROPERTIES_SUFFIX);
  }

  public static void sortXmls(List<Resource> resList) {
    sortResources(resList, XML_SUFFIX);
  }

  public static List<Resource> getSortedResources(String locationPattern, String suffix) {
    if (StringUtils.isEmpty(locationPattern)) {
      throw new RuntimeException("Resource path must not be null or empty");
    }

    if (!locationPattern.endsWith(suffix)) {
      throw new RuntimeException("Resource path must ends with " + suffix);
    }

    String prefix = locationPattern.substring(0, locationPattern.length() - suffix.length());

    List<Resource> resList = PaaSResourceUtils.getResources(locationPattern, prefix + ".*" + suffix);
    sortResources(resList, suffix);
    return resList;
  }

  public static List<Resource> getSortedPorperties(String locationPattern) {
    return getSortedResources(locationPattern, PROPERTIES_SUFFIX);
  }

  public static Properties loadMergedProperties(String locationPattern) throws Exception {
    PropertiesLoader loader = new PropertiesLoader(Arrays.asList(locationPattern));
    return loader.load();
  }

  public static List<Resource> getSortedXmls(String locationPattern) {
    return getSortedResources(locationPattern, XML_SUFFIX);
  }

  @SuppressWarnings("unchecked")
  public static <T> T loadConfigAs(String configId, Class<?> clazz) throws Exception {
    Object config = ConfigMgr.INSTANCE.getConfig(configId);
    if (Properties.class.isInstance(config)) {
      return (T) JsonUtils.convertValue(config, clazz);
    }

    if (Document.class.isInstance(config)) {
      JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (T) jaxbUnmarshaller.unmarshal((Document) config, clazz).getValue();
    }

    throw new Exception("not support");
  }
}
