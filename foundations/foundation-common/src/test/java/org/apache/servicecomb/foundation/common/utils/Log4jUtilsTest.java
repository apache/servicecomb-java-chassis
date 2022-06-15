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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.apache.servicecomb.foundation.common.Holder;
import org.apache.servicecomb.foundation.common.config.impl.PropertiesLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;

public class Log4jUtilsTest {

  @BeforeEach
  public void before() {
    Deencapsulation.setField(Log4jUtils.class, "inited", false);
  }

  @Test
  public void init() {
    Holder<Boolean> propertiesLoaded = new Holder<>(false);
    Holder<Boolean> logConfigured = new Holder<>(false);
    Holder<Boolean> mergedFileWritten = new Holder<>(false);
    final Properties logProperties = new Properties();
    logProperties.setProperty("paas.logs.file", "cse.log");
    final ArrayList<Resource> logResList = new ArrayList<>();
    new MockUp<PropertiesLoader>() {
      @Mock
      Properties load() {
        propertiesLoaded.value = true;
        return logProperties;
      }

      @Mock
      List<Resource> getFoundResList() {
        return logResList;
      }
    };
    new MockUp<PropertyConfigurator>() {
      @Mock
      void configure(Properties properties) {
        logConfigured.value = true;
        Assertions.assertSame(properties, logProperties);
      }
    };
    new MockUp<Log4jUtils>() {
      @Mock
      void outputFile(List<Resource> resList, Properties properties) {
        mergedFileWritten.value = true;
        Assertions.assertSame(logResList, resList);
        Assertions.assertSame(logProperties, properties);
      }
    };

    Assertions.assertFalse(Log4jUtils.isInited());
    try {
      Log4jUtils.init();
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
    Assertions.assertTrue(Log4jUtils.isInited());
    Assertions.assertTrue(propertiesLoaded.value);
    Assertions.assertTrue(logConfigured.value);
    Assertions.assertTrue(mergedFileWritten.value);
  }

  @Test
  public void initOnMergedFileOutputDisabled() {
    Holder<Boolean> propertiesLoaded = new Holder<>(false);
    Holder<Boolean> logConfigured = new Holder<>(false);
    Holder<Boolean> mergedFileWritten = new Holder<>(false);
    final Properties logProperties = new Properties();
    logProperties.setProperty("log4j.logger.outputConfig.enabled", "false");
    final ArrayList<Resource> logResList = new ArrayList<>();
    new MockUp<PropertiesLoader>() {
      @Mock
      Properties load() {
        propertiesLoaded.value = true;
        return logProperties;
      }
    };
    new MockUp<PropertyConfigurator>() {
      @Mock
      void configure(Properties properties) {
        logConfigured.value = true;
        Assertions.assertSame(properties, logProperties);
      }
    };
    new MockUp<Log4jUtils>() {
      @Mock
      void outputFile(List<Resource> resList, Properties properties) {
        mergedFileWritten.value = true;
        Assertions.assertSame(logResList, resList);
        Assertions.assertSame(logProperties, properties);
      }
    };

    Assertions.assertFalse(Log4jUtils.isInited());
    try {
      Log4jUtils.init();
    } catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
    Assertions.assertTrue(Log4jUtils.isInited());
    Assertions.assertTrue(propertiesLoaded.value);
    Assertions.assertTrue(logConfigured.value);
    Assertions.assertFalse(mergedFileWritten.value);
  }
}
