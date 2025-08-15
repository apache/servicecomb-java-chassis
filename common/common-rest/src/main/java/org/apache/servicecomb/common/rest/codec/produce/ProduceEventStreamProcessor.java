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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.swagger.invocation.sse.SseEventResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JavaType;

import jakarta.ws.rs.core.MediaType;

public class ProduceEventStreamProcessor implements ProduceProcessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProduceEventStreamProcessor.class);

  public static final List<String> DEFAULT_DELIMITERS = Arrays.asList("\r\n", "\n", "\r");

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

  @Override
  public Object doDecodeResponse(InputStream input, JavaType type) throws Exception {
    String buffer = new String(input.readAllBytes(), StandardCharsets.UTF_8);
    List<String> lines = new ArrayList<>();
    splitStringByDelimiters(buffer, lines);
    SseEventResponseEntity<?> responseEntity = new SseEventResponseEntity<>();
    for (String line : lines) {
      if (line.startsWith("id:")) {
        responseEntity.id(Integer.parseInt(line.substring("id:".length()).trim()));
        continue;
      }
      if (line.startsWith("event:")) {
        responseEntity.event(line.substring("event:".length()).trim());
        continue;
      }
      if (line.startsWith("retry:")) {
        responseEntity.retry(Long.parseLong(line.substring("retry:".length()).trim()));
        continue;
      }
      if (line.startsWith("data:")) {
        responseEntity.data(RestObjectMapperFactory.getRestObjectMapper()
            .readValue(line.substring("data:".length()).trim(), type));
      }
    }
    return responseEntity;
  }

  private void splitStringByDelimiters(String str, List<String> lines) {
    boolean isContainsDelimiters = false;
    for (String split : DEFAULT_DELIMITERS) {
      if (str.contains(split)) {
        isContainsDelimiters = true;
        splitStrings(str.split(split), lines);
      }
    }
    if (!isContainsDelimiters) {
      lines.add(str);
    }
  }

  private void splitStrings(String[] strings, List<String> lines) {
    for (String str : strings) {
      if (StringUtils.isEmpty(str)) {
        continue;
      }
      splitStringByDelimiters(str, lines);
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
}
