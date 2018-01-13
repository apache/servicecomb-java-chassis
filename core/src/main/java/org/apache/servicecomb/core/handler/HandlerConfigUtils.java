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

package org.apache.servicecomb.core.handler;

import java.util.List;

import org.apache.servicecomb.core.handler.config.Config;
import org.apache.servicecomb.foundation.common.config.PaaSResourceUtils;
import org.apache.servicecomb.foundation.common.config.impl.XmlLoaderUtils;
import org.springframework.core.io.Resource;

public final class HandlerConfigUtils {
  private HandlerConfigUtils() {
  }

  private static Config loadConfig() throws Exception {
    Config config = new Config();

    List<Resource> resList =
        PaaSResourceUtils.getSortedResources("classpath*:config/cse.handler.xml", ".handler.xml");
    for (Resource res : resList) {
      Config tmpConfig = XmlLoaderUtils.load(res, Config.class);
      config.mergeFrom(tmpConfig);
    }

    return config;
  }

  public static void init() throws Exception {
    Config config = loadConfig();
    ConsumerHandlerManager.INSTANCE.init(config);
    ProducerHandlerManager.INSTANCE.init(config);
  }
}
