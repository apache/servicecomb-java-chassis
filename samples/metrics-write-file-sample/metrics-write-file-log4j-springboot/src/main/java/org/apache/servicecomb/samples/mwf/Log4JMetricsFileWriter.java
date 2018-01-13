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

package org.apache.servicecomb.samples.mwf;

import java.nio.file.Paths;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

@Component
public class Log4JMetricsFileWriter implements MetricsFileWriter {
  private static final String METRICS_FILE_ROLLING_MAX_FILE_COUNT = "servicecomb.metrics.file.rolling.max_file_count";

  private static final String METRICS_FILE_ROLLING_MAX_FILE_SIZE = "servicecomb.metrics.file.rolling.max_file_size";

  private static final String METRICS_FILE_ROOT_PATH = "servicecomb.metrics.file.root_path";


  private final Map<String, RollingFileAppender> fileAppenders = new ConcurrentHashMapEx<>();

  private final int maxFileCount;

  private final String maxFileSize;

  private final String rootPath;

  public Log4JMetricsFileWriter() {
    maxFileCount = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_FILE_ROLLING_MAX_FILE_COUNT, 10).get();
    maxFileSize = DynamicPropertyFactory.getInstance()
        .getStringProperty(METRICS_FILE_ROLLING_MAX_FILE_SIZE, "10MB")
        .get();
    rootPath = DynamicPropertyFactory.getInstance().getStringProperty(METRICS_FILE_ROOT_PATH, "target").get();
  }

  @Override
  public void write(String loggerName, String filePrefix, String content) {
    RollingFileAppender logger = fileAppenders.computeIfAbsent(loggerName, f -> initLogger(loggerName, filePrefix));
    LoggingEvent event = new LoggingEvent(loggerName, Logger.getLogger(loggerName), Level.ALL,
        content, null);
    logger.append(event);
  }

  private RollingFileAppender initLogger(String loggerName, String filePrefix) {
    String fileName = Paths.get(rootPath, filePrefix + "." + loggerName + ".dat").toString();
    RollingFileAppender fileAppender = new RollingFileAppender();
    fileAppender.setName(loggerName);
    fileAppender.setFile(fileName);
    fileAppender.setLayout(new PatternLayout("%m%n"));
    fileAppender.setAppend(true);
    fileAppender.setMaxFileSize(maxFileSize);
    fileAppender.setMaxBackupIndex(maxFileCount);
    fileAppender.activateOptions();
    return fileAppender;
  }
}
