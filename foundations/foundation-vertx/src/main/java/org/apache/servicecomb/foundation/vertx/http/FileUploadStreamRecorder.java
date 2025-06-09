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

package org.apache.servicecomb.foundation.vertx.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.netflix.config.DynamicPropertyFactory;

public class FileUploadStreamRecorder {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadStreamRecorder.class);

  private static final FileUploadStreamRecorder RECORDER = new FileUploadStreamRecorder();

  private static final String STREAM_OPEN_UPPER_LIMIT = "file.upload.stream.operate.upper-limit";

  private static final String STREAM_STACKTRACE_ENABLED = "file.upload.stream.operate.stack-trace-enabled";

  private static final String STREAM_CHECK_INTERVAL = "file.upload.stream.operate.check-interval";

  private static final int DEFAULT_STREAM_OPEN_UPPER_LIMIT = 1000;

  private static final long DEFAULT_STREAM_CHECK_INTERVAL = 30000L;

  private final Map<InputStreamWrapper, StreamOperateEvent> wrapperRecorder = new ConcurrentHashMap<>();

  private final EventBus eventBus;

  private final ScheduledExecutorService streamCheckExecutor;

  private FileUploadStreamRecorder() {
    eventBus = EventManager.getEventBus();
    streamCheckExecutor = Executors.newScheduledThreadPool(1, (t) -> new Thread(t, "stream-operate-check"));
    startCheckOpenStream();
  }

  private void startCheckOpenStream() {
    streamCheckExecutor.scheduleWithFixedDelay(this::checkOpenInputStream, DEFAULT_STREAM_CHECK_INTERVAL,
        getStreamCheckInterval(), TimeUnit.MILLISECONDS);
  }

  public static FileUploadStreamRecorder getInstance() {
    return RECORDER;
  }

  public void recordOpenStream(final InputStreamWrapper wrapper) {
    checkAndRemoveOldestStream();
    wrapperRecorder.put(wrapper, new StreamOperateEvent(wrapper));
  }

  private void checkAndRemoveOldestStream() {
    int upperLimit = getStreamOpenUpperLimit();
    if (wrapperRecorder.size() < upperLimit) {
      return;
    }
    List<StreamOperateEvent> operateEvents = new ArrayList<>(wrapperRecorder.values());
    List<StreamOperateEvent> sortEvents = operateEvents.stream()
        .sorted(Comparator.comparingLong(StreamOperateEvent::getOpenStreamTimestamp)).collect(Collectors.toList());
    StreamOperateEvent deleteEvent = sortEvents.get(0);
    LOGGER.warn("reached upper limit [{}] of open stream, delete oldest stream, operate time [{}], stackTrace {}",
        upperLimit, deleteEvent.getOpenStreamTimestamp(), deleteEvent.getInvokeStackTrace());
    closeStreamWrapper(deleteEvent.getInputStreamWrapper());
  }

  public void clearRecorder(InputStreamWrapper inputStreamWrapper) {
    wrapperRecorder.remove(inputStreamWrapper);
  }

  private void checkOpenInputStream() {
    if (wrapperRecorder.isEmpty()) {
      return;
    }
    List<InputStreamWrapper> closeStreamWrapper = new ArrayList<>();
    for (Map.Entry<InputStreamWrapper, StreamOperateEvent> entry : wrapperRecorder.entrySet()) {
      StreamOperateEvent event = entry.getValue();
      long streamOperateTime = event.getOpenStreamTimestamp();
      long notifyTime = getStreamCheckInterval();

      // If the check time exceeds three times, close the open stream.
      if (System.currentTimeMillis() - streamOperateTime >= 3 * notifyTime) {
        closeStreamWrapper.add(entry.getKey());
        continue;
      }
      if (System.currentTimeMillis() - streamOperateTime >= notifyTime) {
        LOGGER.warn("there have input stream not closed, operate time [{}], operate stackTrace {}",
            event.getOpenStreamTimestamp(), event.getInvokeStackTrace());
        eventBus.post(event);
      }
    }
    for (InputStreamWrapper wrapper : closeStreamWrapper) {
      closeStreamWrapper(wrapper);
      LOGGER.warn("closed notify three times stream, operate time [{}], operate stackTrace {}",
          wrapperRecorder.get(wrapper).getOpenStreamTimestamp(), wrapperRecorder.get(wrapper).getInvokeStackTrace());
    }
  }

  private void closeStreamWrapper(InputStreamWrapper wrapper) {
    try {
      wrapper.close();
    } catch (IOException e) {
      LOGGER.error("closed input stream failed!", e);
    }
  }

  private int getStreamOpenUpperLimit() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty(STREAM_OPEN_UPPER_LIMIT, DEFAULT_STREAM_OPEN_UPPER_LIMIT).get();
  }

  private boolean getStackTraceEnabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(STREAM_STACKTRACE_ENABLED, true).get();
  }

  private long getStreamCheckInterval() {
    return DynamicPropertyFactory.getInstance()
        .getLongProperty(STREAM_CHECK_INTERVAL, DEFAULT_STREAM_CHECK_INTERVAL).get();
  }

  private class StreamOperateEvent {
    private final InputStreamWrapper inputStreamWrapper;

    private final long openStreamTimestamp;

    private Exception invokeStackTrace;

    public StreamOperateEvent(InputStreamWrapper inputStreamWrapper) {
      this.inputStreamWrapper = inputStreamWrapper;
      if (getStackTraceEnabled()) {
        this.invokeStackTrace = new Exception();
      }
      this.openStreamTimestamp = System.currentTimeMillis();
    }

    public InputStreamWrapper getInputStreamWrapper() {
      return inputStreamWrapper;
    }

    public Exception getInvokeStackTrace() {
      return invokeStackTrace;
    }

    public long getOpenStreamTimestamp() {
      return openStreamTimestamp;
    }
  }
}
