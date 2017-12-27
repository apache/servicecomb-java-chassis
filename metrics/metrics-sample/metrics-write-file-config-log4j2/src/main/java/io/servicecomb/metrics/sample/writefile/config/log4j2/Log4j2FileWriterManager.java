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

package io.servicecomb.metrics.sample.writefile.config.log4j2;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.springframework.stereotype.Component;

import com.netflix.config.DynamicPropertyFactory;

import io.servicecomb.metrics.sample.writefile.config.FileWriterManager;

@Component
public class Log4j2FileWriterManager implements FileWriterManager {
  private static final String METRICS_FILE_ROLLING_MAXFILECOUNT = "servicecomb.metrics.file.rolling.max_file_count";

  private static final String METRICS_FILE_ROLLING_MAXFILESIZE = "servicecomb.metrics.file.rolling.max_file_size";

  private static final String METRICS_FILE_ROOTPATH = "servicecomb.metrics.file.root_path";

  private final Map<String, RollingFileAppender> fileAppenders = new ConcurrentHashMap<>();

  private final int maxFileCount;

  private final String maxFileSize;

  private final String rootPath;

  public Log4j2FileWriterManager() {
    maxFileCount = DynamicPropertyFactory.getInstance().getIntProperty(METRICS_FILE_ROLLING_MAXFILECOUNT, 10).get();
    maxFileSize = DynamicPropertyFactory.getInstance().getStringProperty(METRICS_FILE_ROLLING_MAXFILESIZE, "10MB").get();
    rootPath = DynamicPropertyFactory.getInstance().getStringProperty(METRICS_FILE_ROOTPATH, "target").get();
  }

  @Override
  public void write(String loggerName, String filePerfix, String content) {
    RollingFileAppender appender = fileAppenders.computeIfAbsent(loggerName, f -> initLogger(loggerName, filePerfix));
    appender.append(Log4jLogEvent.newBuilder().setMessage(new SimpleMessage(content)).build());
  }

  private RollingFileAppender initLogger(String loggerName, String filePerfix) {
    String fileName = Paths.get(rootPath, filePerfix + loggerName + ".dat").toString();
    String filePattern = Paths.get(rootPath, filePerfix + loggerName + "-%i.dat").toString();

    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    return RollingFileAppender.newBuilder().withName(loggerName)
        .withLayout(PatternLayout.newBuilder().withPattern(PatternLayout.DEFAULT_CONVERSION_PATTERN).build())
        .withFileName(fileName)
        .withFilePattern(filePattern)
        .withPolicy(SizeBasedTriggeringPolicy.createPolicy(maxFileSize))
        .withStrategy(
            DefaultRolloverStrategy.createStrategy(String.valueOf(maxFileCount), null, null, null, null, false, config))
        .build();
  }
}
