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

package org.apache.servicecomb.foundation.common.spring;

import java.util.Map.Entry;
import java.util.Properties;

import org.apache.servicecomb.foundation.common.config.PaaSResourceUtils;
import org.apache.servicecomb.foundation.common.utils.FortifyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

//根据各个jar提供的信息来注册parser
public class PaasNamespaceHandler extends NamespaceHandlerSupport {
  private static final Logger LOGGER = LoggerFactory.getLogger(PaasNamespaceHandler.class);

  private static final String NAMESPACE_RES = "classpath*:META-INF/spring/namespace.properties";

  // @Override
  public void init() {
    Properties properties = null;

    try {
      properties = PaaSResourceUtils.loadMergedProperties(NAMESPACE_RES);
    } catch (Exception e) {
      LOGGER.error("Failed to load namespace handler define, {}, {}",
          NAMESPACE_RES,
          FortifyUtils.getErrorInfo(e));
      return;
    }

    for (Entry<Object, Object> entry : properties.entrySet()) {
      String className = entry.getValue().toString();
      try {
        Class<?> clazz = Class.forName(className);
        Object instance = clazz.newInstance();
        registerBeanDefinitionParser(entry.getKey().toString(),
            (BeanDefinitionParser) instance);
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        // 类找不到，说明没部署相应的jar包，这不一定是错误
        // 可能是业务就选择不部署相应的jar包
        // 所以只是打印个info日志

        LOGGER.info("Failed to create BeanDefinitionParser instance of {}", className);
      }
    }
  }
}
