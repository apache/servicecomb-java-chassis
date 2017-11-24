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

package io.servicecomb.foundation.metrics.output.file;

import com.netflix.config.DynamicPropertyFactory;

public abstract class MetricsFileOutput {

  public static final String METRICS_POLL_TIME = "servicecomb.metrics.polltime";
  public static final String METRICS_FILE_ENABLED = "servicecomb.metrics.file.enabled";
  public static final String METRICS_FILE_PATH = "servicecomb.metrics.file.path";
  public static final String METRICS_FILE_Size = "servicecomb.metrics.file.size";

  private final int metricPoll;
  private final String filePath;
  private final String fileSize;

  public int getMetricPoll() {
    return metricPoll;
  }

  public String getFilePath() {
    return filePath;
  }

  public String getFileSize() {
    return fileSize;
  }

  public MetricsFileOutput(){
    filePath = DynamicPropertyFactory.getInstance().getStringProperty(METRICS_FILE_PATH, "").get();
    fileSize = DynamicPropertyFactory.getInstance().getStringProperty(METRICS_FILE_Size, "10MB").get();
    metricPoll = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_POLL_TIME, 30).get();
  }

  public abstract void init();

  public boolean isEnabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(METRICS_FILE_ENABLED, false).get();
  }
}
