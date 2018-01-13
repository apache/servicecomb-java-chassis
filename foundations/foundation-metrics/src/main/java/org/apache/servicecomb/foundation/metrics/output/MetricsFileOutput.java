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

package org.apache.servicecomb.foundation.metrics.output;

import java.util.Map;

import com.netflix.config.DynamicPropertyFactory;

public abstract class MetricsFileOutput {
  public static final String METRICS_FILE_ROOT_PATH = "servicecomb.metrics.file.root_path";

  public static final String METRICS_FILE_NAME_PREFIX = "servicecomb.metrics.file.name_prefix";

  public static final String METRICS_FILE_MAX_ROLLING_SIZE = "servicecomb.metrics.file.max_rolling_size";

  public static final String METRICS_FILE_MAX_ROLLING_COUNT = "servicecomb.metrics.file.max_rolling_count";


  private final String rollingRootFilePath;

  private final String maxRollingFileSize;

  private final int maxRollingFileCount;

  private final String namePrefix;

  public String getRollingRootFilePath() {
    return rollingRootFilePath;
  }

  public String getMaxRollingFileSize() {
    return maxRollingFileSize;
  }

  public int getMaxRollingFileCount() {
    return maxRollingFileCount;
  }

  public String getNamePrefix() {
    return namePrefix;
  }

  public MetricsFileOutput() {
    rollingRootFilePath = DynamicPropertyFactory.getInstance()
        .getStringProperty(METRICS_FILE_ROOT_PATH, "target")
        .get();
    namePrefix = DynamicPropertyFactory.getInstance().getStringProperty(METRICS_FILE_NAME_PREFIX, "metrics").get();
    maxRollingFileSize = DynamicPropertyFactory.getInstance()
        .getStringProperty(METRICS_FILE_MAX_ROLLING_SIZE, "10MB")
        .get();
    maxRollingFileCount = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_FILE_MAX_ROLLING_COUNT, 10).get();
  }

  public abstract void output(Map<String, String> metrics);
}
