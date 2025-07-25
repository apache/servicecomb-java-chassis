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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.swagger.sse.SseEventResponseEntity;

import com.fasterxml.jackson.databind.JavaType;

import jakarta.ws.rs.core.MediaType;

public class ProduceEventStreamProcessor implements ProduceProcessor {
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
    StringBuilder bufferBuilder = new StringBuilder();
    if (result instanceof SseEventResponseEntity<?> responseEntity) {
      appendEventId(bufferBuilder, responseEntity.getEventId());
      appendEvent(bufferBuilder, responseEntity.getEvent());
      appendRetry(bufferBuilder, responseEntity.getRetry());
      appendData(bufferBuilder, responseEntity.getData());
    } else {
      appendEventId(bufferBuilder, writeIndex++);
      appendData(bufferBuilder, result);
    }
    bufferBuilder.append("\n");
    output.write(bufferBuilder.toString().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Object doDecodeResponse(InputStream input, JavaType type) throws Exception {
    String buffer = new String(input.readAllBytes(), StandardCharsets.UTF_8);
    SseEventResponseEntity<?> responseEntity = new SseEventResponseEntity<>();
    for (String line : buffer.split("\n")) {
      if (line.startsWith("eventId: ")) {
        responseEntity.eventId(Integer.parseInt(line.substring(9)));
        continue;
      }
      if (line.startsWith("event: ")) {
        responseEntity.event(line.substring(7));
        continue;
      }
      if (line.startsWith("retry: ")) {
        responseEntity.retry(Long.parseLong(line.substring(7)));
        continue;
      }
      if (line.startsWith("data: ")) {
        responseEntity.data(RestObjectMapperFactory.getRestObjectMapper().readValue(line.substring(6), type));
      }
    }
    if (isNotResponseEntity(responseEntity)) {
      writeIndex++;
      return responseEntity.getData();
    }
    return responseEntity;
  }

  private boolean isNotResponseEntity(SseEventResponseEntity<?> responseEntity) {
    return StringUtils.isEmpty(responseEntity.getEvent())
        && responseEntity.getRetry() == null
        && (responseEntity.getEventId() != null && responseEntity.getEventId() == writeIndex);
  }

  @Override
  public void refreshEventId(int index) {
    this.writeIndex = index;
  }

  private void appendEventId(StringBuilder eventBuilder, Integer eventId) {
    if (eventId == null) {
      return;
    }
    eventBuilder.append("eventId: ").append(eventId.intValue()).append("\n");
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

  private void appendData(StringBuilder eventBuilder, Object data) throws Exception {
    if (data == null) {
      throw new Exception("sse response data is null!");
    }
    eventBuilder.append("data: ")
        .append(RestObjectMapperFactory.getRestObjectMapper().writeValueAsString(data))
        .append("\n");
  }
}
