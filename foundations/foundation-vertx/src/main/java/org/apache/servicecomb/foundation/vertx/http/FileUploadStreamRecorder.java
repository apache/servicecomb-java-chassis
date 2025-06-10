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

  private static final String STREAM_RECORDER_MAX_SIZE = "servicecomb.uploads.file.stream-recorder.max-size";

  private static final String STREAM_STACKTRACE_ENABLED
      = "servicecomb.uploads.file.stream-recorder.stack-trace-enabled";

  private static final String STREAM_CHECK_INTERVAL = "servicecomb.uploads.file.stream-recorder.check-interval";

  private static final int DEFAULT_STREAM_RECORDER_MAX_SIZE = 5000;

  private static final long DEFAULT_STREAM_CHECK_INTERVAL = 30000L;

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
    StreamOperateEvent oldestEvent = getOldestOperateEvent(streamWrapperRecorder.values());
    LOGGER.warn("reached recorder maxSize [{}] of file stream, delete oldest stream, operate time [{}], stackTrace {}",
        maxSize, oldestEvent.getOpenStreamTimestamp(), oldestEvent.getInvokeStackTrace());
    closeStreamWrapper(oldestEvent.getInputStreamWrapper());
  }

  private StreamOperateEvent getOldestOperateEvent(Collection<StreamOperateEvent> values) {
    synchronized (lock) {
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
  }

  public void clearRecorder(InputStreamWrapper inputStreamWrapper) {
    streamWrapperRecorder.remove(inputStreamWrapper);
  }

  private void checkRecordFileStream() {
    if (streamWrapperRecorder.isEmpty()) {
      return;
    }
    List<InputStreamWrapper> overdueStreams = new ArrayList<>();
    for (Map.Entry<InputStreamWrapper, StreamOperateEvent> entry : streamWrapperRecorder.entrySet()) {
      StreamOperateEvent event = entry.getValue();
      long streamOperateTime = event.getOpenStreamTimestamp();
      long notifyTime = getStreamCheckInterval();

      // If the check time exceeds three times, close the open stream.
      if (System.currentTimeMillis() - streamOperateTime >= 3 * notifyTime) {
        overdueStreams.add(entry.getKey());
        continue;
      }
      if (System.currentTimeMillis() - streamOperateTime >= notifyTime) {
        LOGGER.warn("there have file stream not closed, operate time [{}], operate stackTrace {}",
            event.getOpenStreamTimestamp(), event.getInvokeStackTrace());
        eventBus.post(event);
      }
    }
    for (InputStreamWrapper wrapper : overdueStreams) {
      closeStreamWrapper(wrapper);
      LOGGER.warn("closed notify three times stream, operate time [{}], operate stackTrace {}",
          streamWrapperRecorder.get(wrapper).getOpenStreamTimestamp(),
          streamWrapperRecorder.get(wrapper).getInvokeStackTrace());
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

  private boolean getStreamStackTraceEnabled() {
    return DynamicPropertyFactory.getInstance().getBooleanProperty(STREAM_STACKTRACE_ENABLED, false).get();
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
  }
}
