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

package org.apache.servicecomb.provider.pojo;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.config.DynamicPropertyFactory;

@Configuration
class PojoConfig {

  private static final String LOADING_MODE_BLOCKING = "blocking";

  @Bean
  Executor executor() {
    if (LOADING_MODE_BLOCKING.equals(loadingMode())) {
      return Runnable::run;
    }

    return Executors.newSingleThreadExecutor();
  }

  private String loadingMode() {
    return DynamicPropertyFactory.getInstance()
        .getStringProperty(
            "servicecomb.rpcReference.loadingMode",
            LOADING_MODE_BLOCKING)
        .get();
  }
}
