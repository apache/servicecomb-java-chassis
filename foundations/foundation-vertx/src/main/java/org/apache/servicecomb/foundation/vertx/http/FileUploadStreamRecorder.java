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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.event.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.netflix.config.DynamicPropertyFactory;

public class FileUploadStreamRecorder {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadStreamRecorder.class);

  private static final FileUploadStreamRecorder RECORDER = new FileUploadStreamRecorder();

  private static final String STREAM_RECORDER_MAX_SIZE = "servicecomb.uploads.file.streamRecorder.maxSize";

  private static final String STREAM_STACKTRACE_ENABLED
      = "servicecomb.uploads.file.streamRecorder.stackTraceEnabled";

  private static final String STREAM_CHECK_INTERVAL = "servicecomb.uploads.file.streamRecorder.checkInterval";

  private static final String STREAM_MAX_OPEN_TIME = "servicecomb.uploads.file.streamRecorder.streamMaxOpenTime";

  private static final int DEFAULT_STREAM_RECORDER_MAX_SIZE = 5000;

  private static final long DEFAULT_STREAM_CHECK_INTERVAL = 30000L;

  private static final long DEFAULT_STREAM_MAX_OPEN_TIME = 90000L;

  private final Map<InputStreamWrapper, StreamOperateEvent> streamWrapperRecorder = new ConcurrentHashMap<>();

  private final EventBus eventBus;

  private final ScheduledExecutorService streamCheckExecutor;

  private final Object lock = new Object();

  private FileUploadStreamRecorder() {
    eventBus = EventManager.getEventBus();
    streamCheckExecutor = Executors.newScheduledThreadPool(1,
        (t) -> new Thread(t, "upload-file-stream-check"));
    startCheckRecordFileStream();
  }

  private void startCheckRecordFileStream() {
    streamCheckExecutor.scheduleWithFixedDelay(this::checkRecordFileStream, DEFAULT_STREAM_CHECK_INTERVAL,
        getStreamCheckInterval(), TimeUnit.MILLISECONDS);
  }

  public static FileUploadStreamRecorder getInstance() {
    return RECORDER;
  }

  public void recordOpenStream(final InputStreamWrapper wrapper) {
    checkAndRemoveOldestStream();
    streamWrapperRecorder.put(wrapper, new StreamOperateEvent(wrapper));
  }

  private void checkAndRemoveOldestStream() {
    int maxSize = getStreamRecorderMaxSize();
    if (streamWrapperRecorder.size() < maxSize) {
      return;
    }
    synchronized (lock) {
      StreamOperateEvent oldestEvent = getOldestOperateEvent(streamWrapperRecorder.values());
      LOGGER.warn("reached record maxSize [{}] of file stream, delete oldest stream, operate time [{}], stackTrace: ",
          maxSize, oldestEvent.getOpenStreamTimestamp(), oldestEvent.getInvokeStackTrace());
      oldestEvent.setEventType(EventType.OVER_SIZE);
      eventBus.post(oldestEvent);
      closeStreamWrapper(oldestEvent.getInputStreamWrapper());
    }
  }

  private StreamOperateEvent getOldestOperateEvent(Collection<StreamOperateEvent> values) {
    StreamOperateEvent oldestEvent = null;
    for (StreamOperateEvent event : values) {
      if (oldestEvent == null) {
        oldestEvent = event;
        continue;
      }
      if (oldestEvent.getOpenStreamTimestamp() > event.getOpenStreamTimestamp()) {
        oldestEvent = event;
      }
    }
    return oldestEvent;
  }

  public void clearRecorder(InputStreamWrapper inputStreamWrapper) {
    streamWrapperRecorder.remove(inputStreamWrapper);
  }

  private void checkRecordFileStream() {
    try {
      if (streamWrapperRecorder.isEmpty()) {
        return;
      }
      List<StreamOperateEvent> overdueStreamEvents = new ArrayList<>();
      long currentMillis = System.currentTimeMillis();
      for (StreamOperateEvent event : streamWrapperRecorder.values()) {
        long streamOperateTime = event.getOpenStreamTimestamp();
        if (currentMillis - streamOperateTime >= getStreamMaxOpenTime()) {
          overdueStreamEvents.add(event);
        }
      }
      for (StreamOperateEvent overdueEvent : overdueStreamEvents) {
        overdueEvent.setEventType(EventType.TIMEOUT);
        eventBus.post(overdueEvent);
        closeStreamWrapper(overdueEvent.getInputStreamWrapper());
        LOGGER.warn("closed timeout stream, operate time [{}], operate stackTrace: ",
            overdueEvent.getOpenStreamTimestamp(), overdueEvent.getInvokeStackTrace());
      }
    } catch (Exception e) {
      LOGGER.error("checkRecordFileStream failed, next interval will try again.", e);
    }
  }

  private void closeStreamWrapper(InputStreamWrapper wrapper) {
    try {
      wrapper.close();
    } catch (IOException e) {
      LOGGER.error("closed input stream failed!", e);
    }
  }

  private int getStreamRecorderMaxSize() {
    return DynamicPropertyFactory.getInstance()
        .getIntProperty(STREAM_RECORDER_MAX_SIZE, DEFAULT_STREAM_RECORDER_MAX_SIZE).get();
  }

  private static boolean getStreamStackTraceEnabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(STREAM_STACKTRACE_ENABLED, false).get();
  }

  private long getStreamCheckInterval() {
    return DynamicPropertyFactory.getInstance()
        .getLongProperty(STREAM_CHECK_INTERVAL, DEFAULT_STREAM_CHECK_INTERVAL).get();
  }

  private long getStreamMaxOpenTime() {
    return DynamicPropertyFactory.getInstance()
        .getLongProperty(STREAM_MAX_OPEN_TIME, DEFAULT_STREAM_MAX_OPEN_TIME).get();
  }

  public static class StreamOperateEvent {
    private final InputStreamWrapper inputStreamWrapper;

    private final long openStreamTimestamp;

    private Exception invokeStackTrace;

    private EventType eventType;

    public StreamOperateEvent(InputStreamWrapper inputStreamWrapper) {
      this.inputStreamWrapper = inputStreamWrapper;
      if (getStreamStackTraceEnabled()) {
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

    public EventType getEventType() {
      return eventType;
    }

    public void setEventType(EventType eventType) {
      this.eventType = eventType;
    }
  }

  public enum EventType {
    OVER_SIZE,
    TIMEOUT
  }
}
