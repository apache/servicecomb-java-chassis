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
package org.apache.servicecomb.foundation.test.scaffolding.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LogCollector implements Closeable {
  List<LogEvent> events = new ArrayList<>();

  Appender appender = new AbstractAppender("LogCollector", null, PatternLayout.createDefaultLayout()) {
    @Override
    public void append(LogEvent event) {
      events.add(event);
    }
  };

  public LogCollector() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    appender.start();
    config.getRootLogger().addAppender(appender, Level.ALL, null);
    ctx.updateLoggers(config);
  }

  public LogCollector setLogLevel(String logName, Level level) {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    config.getLoggerConfig(logName).setLevel(level);
    return this;
  }

  public List<LogEvent> getEvents() {
    return events;
  }

  public LogEvent getLastEvents() {
    return events.get(events.size() - 1);
  }

  public List<Throwable> getThrowables() {
    return events.stream()
        .map(LogEvent::getThrown)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public List<String> getThrowableMessages() {
    return events.stream()
        .filter(e -> e.getThrown() != null)
        .map(e -> e.getThrown().getMessage())
        .collect(Collectors.toList());
  }

  public void teardown() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    appender.stop();
    config.getRootLogger().removeAppender("LogCollector");
    ctx.updateLoggers(config);
  }

  public void clear() {
    events = new ArrayList<>();
  }

  @Override
  public void close() {
    teardown();
  }
}
