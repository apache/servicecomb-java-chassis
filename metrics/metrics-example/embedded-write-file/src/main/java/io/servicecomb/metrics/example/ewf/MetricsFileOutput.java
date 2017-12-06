/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.example.ewf;

import java.util.Map;

import com.netflix.config.DynamicPropertyFactory;

public abstract class MetricsFileOutput {
  private static final String METRICS_FILE_ROOT_PATH = "servicecomb.metrics.example.ewf.root_path";

  private static final String METRICS_FILE_NAME_PREFIX = "servicecomb.metrics.example.ewf.name_prefix";

  private static final String METRICS_FILE_MAX_ROLLING_SIZE = "servicecomb.metrics.example.ewf.max_rolling_size";

  private static final String METRICS_FILE_MAX_ROLLING_COUNT = "servicecomb.metrics.example.ewf.max_rolling_count";

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
    rollingRootFilePath = DynamicPropertyFactory.getInstance().getStringProperty(METRICS_FILE_ROOT_PATH, "target")
        .get();
    namePrefix = DynamicPropertyFactory.getInstance().getStringProperty(METRICS_FILE_NAME_PREFIX, "metrics").get();
    maxRollingFileSize = DynamicPropertyFactory.getInstance().getStringProperty(METRICS_FILE_MAX_ROLLING_SIZE, "10MB")
        .get();
    maxRollingFileCount = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_FILE_MAX_ROLLING_COUNT, 10).get();
  }

  public abstract void output(Map<String, String> metrics);
}
