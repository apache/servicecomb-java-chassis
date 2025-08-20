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

package org.apache.servicecomb.common.rest.codec.produce;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.foundation.vertx.stream.BufferInputStream;
import org.apache.servicecomb.swagger.invocation.sse.SseEventResponseEntity;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.reactivex.rxjava3.core.Flowable;
import io.vertx.core.buffer.Buffer;
import jakarta.ws.rs.core.MediaType;

public class ProduceEventStreamProcessor implements ProduceProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProduceEventStreamProcessor.class);

  private static final String CR_STR = "\r";

  private static final byte[] CR = CR_STR.getBytes(StandardCharsets.UTF_8);

  private static final String LF_STR = "\n";

  private static final byte[] LF = LF_STR.getBytes(StandardCharsets.UTF_8);

  private static final String CRLF_STR = "\r\n";

  private static final byte[] CRLF = CRLF_STR.getBytes(StandardCharsets.UTF_8);

  private String lineDelimiter;

  private byte[] lineDelimiterBytes;

  private int writeIndex = 0;

  @Override
  public String getName() {
    return MediaType.SERVER_SENT_EVENTS;
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public void doEncodeResponse(OutputStream output, Object result) throws Exception {
    StringBuilder eventBuilder = new StringBuilder();
    if (result instanceof SseEventResponseEntity<?> responseEntity) {
      appendId(eventBuilder, responseEntity.getId());
      appendEvent(eventBuilder, responseEntity.getEvent());
      appendRetry(eventBuilder, responseEntity.getRetry());
      appendData(eventBuilder, responseEntity.getData());
      eventBuilder.append("\n");
      output.write(eventBuilder.toString().getBytes(StandardCharsets.UTF_8));
    } else {
      LOGGER.warn("Does not support encoding objects other than SseEventResponseEntity!");
    }
  }

  private enum ProcessStatus {
    DETERMINE_LINE_DELIMITER,
    MATCHING_CR,
    MATCHING_LF,
    MATCHING_CRLF,
    MATCHING_LINE,
    END_OF_MESSAGE,
    /**
     * The whole SSE stream is closed.
     * Be careful: there may be remaining buffer should be processed.
     */
    END_OF_STREAM
  }

  private ProcessStatus loopStatus = ProcessStatus.DETERMINE_LINE_DELIMITER;

  private int matchingDelimiterIndex = 0;

  final ByteBuf buffer = Unpooled.buffer();

  private SseEventResponseEntity<?> currentEntity = new SseEventResponseEntity<>();

  private List<SseEventResponseEntity<?>> entityList = new ArrayList<>();

  private JavaType type;

  @Override
  public List<SseEventResponseEntity<?>> doDecodeResponse(InputStream input, JavaType type) throws Exception {
    this.type = type;
    final byte[] readCache = new byte[Math.min(128, input.available())];
    int bytesRead;
    while ((bytesRead = input.read(readCache)) > 0) {
      processAllBytes(readCache, bytesRead);
    }
    final List<SseEventResponseEntity<?>> resultList = entityList;
    entityList = new ArrayList<>();
    return resultList;
  }

  private void processAllBytes(byte[] readCache, int cacheEndPos) {
    int lastProcessedPosition = innerLoop(readCache, 0, cacheEndPos);
    while (lastProcessedPosition < cacheEndPos) {
      lastProcessedPosition = innerLoop(readCache, lastProcessedPosition, cacheEndPos);
    }
  }

  private int innerLoop(final byte[] readCache, final int startPos, final int cacheEndPos) {
    if (startPos >= cacheEndPos) {
      return cacheEndPos;
    }
    switch (loopStatus) {
      case MATCHING_CR -> {
        return tryToMatchDelimiterCR(readCache, startPos, cacheEndPos);
      }
      case MATCHING_CRLF -> {
        return tryToMatchDelimiterCRLF(readCache, startPos, cacheEndPos);
      }
      case MATCHING_LF -> {
        return tryToMatchDelimiterLF(readCache, startPos, cacheEndPos);
      }
      case DETERMINE_LINE_DELIMITER -> {
        return searchFirstLineDelimiter(readCache, startPos, cacheEndPos);
      }
      case MATCHING_LINE -> {
        return bufferReadCacheAndProcessLines(readCache, startPos, cacheEndPos);
      }
      case END_OF_STREAM -> {
        return processLeftBuffer(cacheEndPos);
      }
      default -> throw new IllegalStateException("unexpected case");
    }
  }

  private int processLeftBuffer(int cacheEndPos) {
    final byte[] bytes = readAllBytesFromBuffer(buffer);
    final String bufferStr = new String(bytes, StandardCharsets.UTF_8);
    processStringBuffer(bufferStr);
    return cacheEndPos;
  }

  private int bufferReadCacheAndProcessLines(byte[] readCache, int startPos, int cacheEndPos) {
    buffer.writeBytes(readCache, startPos, cacheEndPos - startPos);
    processAllAvailableBufferLines();
    return cacheEndPos;
  }

  private int tryToMatchDelimiterCR(byte[] readCache, int startPos, int cacheEndPos) {
    int bytesProcessed = 0;
    for (; matchingDelimiterIndex < CR.length && startPos + bytesProcessed < cacheEndPos; ++bytesProcessed) {
      if (readCache[startPos + bytesProcessed] == CR[matchingDelimiterIndex]) {
        buffer.writeByte(readCache[startPos + bytesProcessed]);
        ++matchingDelimiterIndex;
      } else {
        loopStatus = ProcessStatus.DETERMINE_LINE_DELIMITER;
        matchingDelimiterIndex = 0;
        return startPos + bytesProcessed;
      }
    }
    if (matchingDelimiterIndex == CR.length) {
      // matched all CR bytes, attempting to further match CRLF.
      loopStatus = ProcessStatus.MATCHING_CRLF;
    }
    return startPos + bytesProcessed;
  }

  private int tryToMatchDelimiterCRLF(byte[] readCache, int startPos, int cacheEndPos) {
    // If you enter this branch, it means that at least CR should be used as the line break character.
    int bytesProcessed = 0;
    for (; matchingDelimiterIndex < CRLF.length && startPos + bytesProcessed < cacheEndPos; ++bytesProcessed) {
      if (readCache[startPos + bytesProcessed] == CRLF[matchingDelimiterIndex]) {
        buffer.writeByte(readCache[startPos + bytesProcessed]);
        ++matchingDelimiterIndex;
      } else {
        determineDelimiter(CR_STR, CR);
        return startPos + bytesProcessed;
      }
    }
    if (matchingDelimiterIndex == CRLF.length) {
      determineDelimiter(CRLF_STR, CRLF);
    }
    return startPos + bytesProcessed;
  }

  private int tryToMatchDelimiterLF(byte[] readCache, int startPos, int cacheEndPos) {
    int bytesProcessed = 0;
    for (; matchingDelimiterIndex < LF.length && startPos + bytesProcessed < cacheEndPos; ++bytesProcessed) {
      if (readCache[startPos + bytesProcessed] == LF[matchingDelimiterIndex]) {
        buffer.writeByte(readCache[startPos + bytesProcessed]);
        ++matchingDelimiterIndex;
      } else {
        loopStatus = ProcessStatus.DETERMINE_LINE_DELIMITER;
        matchingDelimiterIndex = 0;
        return startPos + bytesProcessed;
      }
    }
    if (matchingDelimiterIndex == LF.length) {
      determineDelimiter(LF_STR, LF);
    }
    return startPos + bytesProcessed;
  }

  private void determineDelimiter(String delimiterStr, byte[] delimiterBytes) {
    lineDelimiter = delimiterStr;
    lineDelimiterBytes = delimiterBytes;
    matchingDelimiterIndex = 0;
    loopStatus = ProcessStatus.MATCHING_LINE;
  }

  private int searchFirstLineDelimiter(byte[] readCache, int startPos, int cacheEndPos) {
    for (int i = startPos; i < cacheEndPos; ++i) {
      if (readCache[i] == CR[0]) {
        loopStatus = ProcessStatus.MATCHING_CR;
        matchingDelimiterIndex = 0;
        return i;
      } else if (readCache[i] == LF[0]) {
        loopStatus = ProcessStatus.MATCHING_LF;
        matchingDelimiterIndex = 0;
        return i;
      } else {
        buffer.writeByte(readCache[i]);
      }
    }
    return cacheEndPos;
  }

  private void processAllAvailableBufferLines() {
    while (buffer.readableBytes() > 0) {
      final byte[] bytes = readALineOfBytesFromBuffer(buffer);
      if (bytes == null || bytes.length == 0) {
        return;
      }
      final String bufferStr = new String(bytes, StandardCharsets.UTF_8);
      processStringBuffer(bufferStr);
    }
  }

  private void processStringBuffer(String bufferStr) {
    int cursor = 0;
    int delimiterIdx;
    while ((delimiterIdx = bufferStr.indexOf(lineDelimiter, cursor)) >= 0) {
      final String line = bufferStr.substring(cursor, delimiterIdx);
      processStringLine(line);
      cursor = delimiterIdx + lineDelimiter.length();
    }
    if (cursor < bufferStr.length()) {
      buffer.writeBytes(bufferStr.substring(cursor).getBytes(StandardCharsets.UTF_8));
    }
  }

  private void processStringLine(String line) {
    if (StringUtils.isBlank(line)) {
      if (currentEntity.isEmpty()) {
        return;
      }
      entityList.add(currentEntity);
      currentEntity = new SseEventResponseEntity<>();
      return;
    }
    final String[] split = line.split(":", 2);
    if (split.length < 2) {
      LOGGER.error("get a line of sse event without colon! stream is breaking!");
      throw new IllegalStateException("get a line of sse event without colon!");
    }
    switch (split[0]) {
      case "event" -> {
        if (StringUtils.isNotBlank(split[1])) {
          currentEntity.event(split[1].trim());
        }
      }
      case "id" -> {
        if (StringUtils.isNotBlank(split[1])) {
          currentEntity.id(Integer.parseInt(split[1].trim()));
        }
      }
      case "data" -> {
        try {
          currentEntity.data(RestObjectMapperFactory.getRestObjectMapper().readValue(split[1].trim(), type));
        } catch (JsonProcessingException e) {
          LOGGER.error("failed to process data of sse event: [{}]", e.getMessage());
          throw new IllegalStateException("failed to process data of sse event", e);
        }
      }
      case "retry" -> {
        if (StringUtils.isNotBlank(split[1])) {
          currentEntity.retry(Long.parseLong(split[1].trim()));
        }
      }
      default -> {
        LOGGER.debug("unrecognized sse message line! ignored string segment length=[{}]", line.length());
      }
    }
  }

  private byte[] readALineOfBytesFromBuffer(ByteBuf buffer) {
    matchingDelimiterIndex = 0;
    try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.readableBytes())) {
      while (buffer.readableBytes() > 0 && matchingDelimiterIndex < lineDelimiterBytes.length) {
        final byte b = buffer.readByte();
        if (b == lineDelimiterBytes[matchingDelimiterIndex]) {
          ++matchingDelimiterIndex;
        }
        bos.write(b);
      }
      if (matchingDelimiterIndex < lineDelimiterBytes.length) {
        // The newline character was not matched, so this part of the buffer does not constitute a complete line of
        // content and needs to remain in the buffer, waiting for the next segment to arrive for processing.
        buffer.writeBytes(bos.toByteArray());
        return null;
      }
      matchingDelimiterIndex = 0;
      return bos.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("impossible error while closing ByteArrayOutputStream", e);
    }
  }

  private byte[] readAllBytesFromBuffer(ByteBuf buffer) {
    try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(buffer.readableBytes())) {
      buffer.readBytes(bos, buffer.readableBytes());
      return bos.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("impossible error while closing ByteArrayOutputStream", e);
    }
  }

  private void appendId(StringBuilder eventBuilder, Integer eventId) {
    int id = eventId != null ? eventId : writeIndex++;
    eventBuilder.append("id: ").append(id).append("\n");
  }

  private void appendEvent(StringBuilder eventBuilder, String event) {
    if (StringUtils.isEmpty(event)) {
      return;
    }
    eventBuilder.append("event: ").append(event).append("\n");
  }

  private void appendRetry(StringBuilder eventBuilder, Long retry) {
    if (retry == null) {
      return;
    }
    eventBuilder.append("retry: ").append(retry.longValue()).append("\n");
  }

  private void appendData(StringBuilder eventBuilder, List<?> datas) throws Exception {
    if (CollectionUtils.isEmpty(datas)) {
      throw new Exception("sse response data is null!");
    }
    for (Object data : datas) {
      eventBuilder.append("data: ")
          .append(RestObjectMapperFactory.getRestObjectMapper().writeValueAsString(data))
          .append("\n");
    }
  }

  @Override
  public Publisher<SseEventResponseEntity<?>> decodeResponse(Buffer buffer, JavaType type) throws Exception {
    if (buffer.length() == 0) {
      return Flowable.empty();
    }

    try (BufferInputStream input = new BufferInputStream(buffer.getByteBuf())) {
      final List<SseEventResponseEntity<?>> list = doDecodeResponse(input, type);
      return Flowable.fromIterable(list);
    }
  }

  public Publisher<SseEventResponseEntity<?>> close() throws Exception {
    if (type == null) {
      return Flowable.empty();
    }
    try (final ByteArrayInputStream input = new ByteArrayInputStream(
        (lineDelimiter + lineDelimiter).getBytes(StandardCharsets.UTF_8))) {
      // Write two additional newline characters into the buffer to ensure that the processor completes
      // processing all remaining content in the buffer.
      final List<SseEventResponseEntity<?>> list = doDecodeResponse(input, type);
      return Flowable.fromIterable(list);
    }
  }
}
