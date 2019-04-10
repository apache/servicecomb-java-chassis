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

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

public class LogCollector implements Closeable {
  List<LoggingEvent> events = new ArrayList<>();

  Appender appender = new AppenderSkeleton() {
    @Override
    public void append(LoggingEvent event) {
      events.add(event);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
      return false;
    }
  };

  public LogCollector() {
    Logger.getRootLogger().addAppender(appender);
  }

  public LogCollector setLogLevel(String logName, Level level) {
    Logger.getLogger(logName).setLevel(level);
    return this;
  }

  public List<LoggingEvent> getEvents() {
    return events;
  }

  public LoggingEvent getLastEvents() {
    return events.get(events.size() - 1);
  }

  public List<Throwable> getThrowables() {
    return events.stream()
        .filter(e -> e.getThrowableInformation() != null)
        .map(e -> e.getThrowableInformation().getThrowable())
        .collect(Collectors.toList());
  }

  public List<String> getThrowableMessages() {
    return events.stream()
        .filter(e -> e.getThrowableInformation() != null)
        .map(e -> e.getThrowableInformation().getThrowable().getMessage())
        .collect(Collectors.toList());
  }

  public void teardown() {
    Logger.getRootLogger().removeAppender(appender);
  }

  public void clear() {
    events = new ArrayList<>();
  }

  @Override
  public void close() {
    teardown();
  }
}
