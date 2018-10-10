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
package org.apache.servicecomb.it.deploy;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.it.ITUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubProcessLogger implements Closeable {
  private static final Logger LOGGER = LoggerFactory.getLogger(SubProcessLogger.class);

  private final String displayName;

  private BufferedReader reader;

  private Thread thread;

  private String startCompleteLog;

  private volatile boolean startCompleted;

  private List<String> logs = new ArrayList<>();

  public SubProcessLogger(String displayName, InputStream inputStream, String startCompleteLog) {
    this.displayName = displayName;
    this.startCompleteLog = startCompleteLog;

    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
    this.reader = new BufferedReader(new InputStreamReader(bufferedInputStream));

    thread = new Thread(this::run, "SubProcessLogger-" + displayName);
    thread.start();
  }

  private void run() {
    try {
      doRun();
    } catch (IOException e) {
      LOGGER.error("Failed to read log.", e);
    }
  }

  private void doRun() throws IOException {
    String line;
    while ((line = reader.readLine()) != null) {
      logs.add(String.format("[%s] %s", displayName, line));

      checkStartComplete(line);
    }
  }

  public List<String> getAndClearLog() {
    List<String> old = logs;
    logs = new ArrayList<>();
    return old;
  }

  private void checkStartComplete(String line) {
    if (startCompleted || startCompleteLog == null) {
      return;
    }

    startCompleted = line.contains(startCompleteLog);
  }

  public boolean isStartCompleted() {
    return startCompleted;
  }

  public void waitStartComplete() {
    if (startCompleteLog == null) {
      throw new IllegalStateException(
          String.format("[%s] not set startCompleteLog, can not wait start complete.", displayName));
    }

    LOGGER.info("waiting {} start.", displayName);
    long startTime = System.currentTimeMillis();
    for (; ; ) {
      if (startCompleted) {
        LOGGER.info("{} start completed.", displayName);
        return;
      }

      if (System.currentTimeMillis() - startTime > TimeUnit.MINUTES.toMillis(1)) {
        throw new IllegalStateException(String.format("[%s] timeout to wait for start complete.", displayName));
      }

      ITUtils.forceWait(TimeUnit.MILLISECONDS, 500);
    }
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }
}
