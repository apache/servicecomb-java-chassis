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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.swagger.invocation.sse.SseEventResponseEntity;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.fasterxml.jackson.databind.JavaType;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Test ProduceEventStreamProcessor
 *
 * @since 2025-08-19
 */
public class TestProduceEventStreamProcessor {
  @Test
  public void doDecodeResponse() throws Exception {
    doDecodeResponseTemplateComposite("\n");
    doDecodeResponseTemplateComposite("\r");
    doDecodeResponseTemplateComposite("\r\n");
  }

  private void doDecodeResponseTemplateComposite(String lineDelimiter) throws Exception {
    doDecodeResponseTemplate(lineDelimiter, 2);
    doDecodeResponseTemplate(lineDelimiter, 1);
    doDecodeResponseTemplate(lineDelimiter, 0);
  }

  private void doDecodeResponseTemplate(String lineDelimiter, int delimiterInTheFirstSegment) throws Exception {
    final ProduceEventStreamProcessor processor = new ProduceEventStreamProcessor();
    final ByteArrayInputStream stream0 = prepareStream(
        "id: 0" + lineDelimiter + "data: \"aaa\"" + lineDelimiter.repeat(delimiterInTheFirstSegment));
    final Object o0 = processor.doDecodeResponse(stream0,
        RestObjectMapperFactory.getRestObjectMapper().constructType(String.class));
    if (delimiterInTheFirstSegment > 1) {
      checkDecodeResult(o0, new ItemChecker(0, Collections.singletonList("aaa"), null, null));
    } else {
      checkDecodeResult(o0);
    }

    final ByteArrayInputStream stream1 = prepareStream(
        lineDelimiter.repeat(2 - delimiterInTheFirstSegment) + "id: 1" + lineDelimiter + "data: \"bbb\""
            + lineDelimiter);
    final Object o1 = processor.doDecodeResponse(stream1,
        RestObjectMapperFactory.getRestObjectMapper().constructType(String.class));
    if (delimiterInTheFirstSegment > 1) {
      checkDecodeResult(o1);
    } else {
      checkDecodeResult(o1, new ItemChecker(0, Collections.singletonList("aaa"), null, null));
    }

    final ByteArrayInputStream stream2 = prepareStream(
        lineDelimiter + "id: 2" + lineDelimiter + "data: \"ccc\"" + lineDelimiter + "event: test" + lineDelimiter
            + "retry: 123");
    final Object o2 = processor.doDecodeResponse(stream2,
        RestObjectMapperFactory.getRestObjectMapper().constructType(String.class));
    checkDecodeResult(o2,
        new ItemChecker(1, Collections.singletonList("bbb"), null, null));

    final ByteArrayInputStream stream3 = prepareStream(
        lineDelimiter + lineDelimiter + "id: 3" + lineDelimiter + "data: \"ddd\"" + lineDelimiter
            + "event: test3" + lineDelimiter + "retry: 321" + lineDelimiter);
    final Object o3 = processor.doDecodeResponse(stream3,
        RestObjectMapperFactory.getRestObjectMapper().constructType(String.class));
    checkDecodeResult(o3,
        new ItemChecker(2, Collections.singletonList("ccc"), "test", 123L));
    final Object o4 = ((Flowable) processor.close()).toList().blockingGet();
    checkDecodeResult(o4, new ItemChecker(3, Collections.singletonList("ddd"), "test3", 321L));
  }

  @Test
  public void doDecodeResponseMultiData() throws Exception {
    doDecodeResponseMultiDataTemplate("\n");
    doDecodeResponseMultiDataTemplate("\r");
    doDecodeResponseMultiDataTemplate("\r\n");
  }

  private void doDecodeResponseMultiDataTemplate(String lineDelimiter) throws Exception {
    final ProduceEventStreamProcessor processor = new ProduceEventStreamProcessor();

    final ByteArrayInputStream stream0 = prepareStream(
        "id: 0" + lineDelimiter + "data: \"aaa1\"" + lineDelimiter + "data: \"aaa2\"" + lineDelimiter + lineDelimiter);
    final Object o0 = processor.doDecodeResponse(stream0,
        RestObjectMapperFactory.getRestObjectMapper().constructType(String.class));
    checkDecodeResult(o0, new ItemChecker(0, Arrays.asList("aaa1", "aaa2"), null, null));

    final ByteArrayInputStream stream1 = prepareStream(
        "id: 1" + lineDelimiter + "data: \"aaa3\"" + lineDelimiter + "data: \"aaa4\"" + lineDelimiter + "data: \"aaa5\""
            + lineDelimiter + "data: \"aaa6\"");
    final Object o1 = processor.doDecodeResponse(stream1,
        RestObjectMapperFactory.getRestObjectMapper().constructType(String.class));
    checkDecodeResult(o1);
    final Object o2 = ((Flowable) processor.close()).toList().blockingGet();
    checkDecodeResult(o2, new ItemChecker(1, Arrays.asList("aaa3", "aaa4", "aaa5", "aaa6"), null, null));
  }

  @Test
  public void doDecodeResponseMultiPackage() throws Exception {
    doDecodeResponseMultiPackageTemplate("\n");
    doDecodeResponseMultiPackageTemplate("\r");
    doDecodeResponseMultiPackageTemplate("\r\n");
  }

  private void doDecodeResponseMultiPackageTemplate(String lineDelimiter) throws Exception {
    final ProduceEventStreamProcessor processor = new ProduceEventStreamProcessor();

    final ByteArrayInputStream stream0 = prepareStream(
        "id: 0" + lineDelimiter + "data: \"aaa\"" + lineDelimiter + lineDelimiter + lineDelimiter);
    final Object o0 = processor.doDecodeResponse(stream0,
        RestObjectMapperFactory.getRestObjectMapper().constructType(String.class));
    checkDecodeResult(o0, new ItemChecker(0, Collections.singletonList("aaa"), null, null));

    final ByteArrayInputStream stream1 = prepareStream("id: 1" + lineDelimiter + "data: \"bbb\"" + lineDelimiter);
    final Object o1 = processor.doDecodeResponse(stream1,
        RestObjectMapperFactory.getRestObjectMapper().constructType(String.class));
    checkDecodeResult(o1);

    final ByteArrayInputStream stream2 = prepareStream(
        lineDelimiter + "id: 2" + lineDelimiter + "data: \"ccc\"" + lineDelimiter + "event: test" + lineDelimiter
            + "retry: 123" + lineDelimiter + lineDelimiter + "id: 3" + lineDelimiter + "data: \"ddd\"" + lineDelimiter
            + "event: test3" + lineDelimiter + "retry: 321" + lineDelimiter + lineDelimiter);
    final Object o2 = processor.doDecodeResponse(stream2,
        RestObjectMapperFactory.getRestObjectMapper().constructType(String.class));
    checkDecodeResult(o2,
        new ItemChecker(1, Collections.singletonList("bbb"), null, null),
        new ItemChecker(2, Collections.singletonList("ccc"), "test", 123L),
        new ItemChecker(3, Collections.singletonList("ddd"), "test3", 321L));
    final Object o4 = ((Flowable) processor.close()).toList().blockingGet();
    checkDecodeResult(o4);
  }

  @Test
  public void doDecodeResponseHalfPackage() {
    for (int splitIndexesCount = 1; splitIndexesCount < 3; splitIndexesCount++) {
      for (int trailingDelimiterCount = 0; trailingDelimiterCount < 3; trailingDelimiterCount++) {
        doDecodeResponseHalfPackageTemplate("\n", splitIndexesCount, trailingDelimiterCount);
        doDecodeResponseHalfPackageTemplate("\r", splitIndexesCount, trailingDelimiterCount);
        doDecodeResponseHalfPackageTemplate("\r\n", splitIndexesCount, trailingDelimiterCount);
      }
    }
  }

  private void doDecodeResponseHalfPackageTemplate(String lineDelimiter, int splitIndexesCount,
      int trailingDelimiterCount) {
    final String messageTemplate =
        "data: \"中文aaa\"" + lineDelimiter
            + "id: 0" + lineDelimiter
            + lineDelimiter
            + "id: 1" + lineDelimiter
            + "data: \"bbb中文\"" + lineDelimiter
            + "data: \"123汉语\"" + lineDelimiter
            + lineDelimiter
            + "data: \"~!@#$%^&*()_+=-0987654321`中文[]{}\\\\|;':\\\",./<>?abc\"" + lineDelimiter
            + "data: \"文字ccc\"" + lineDelimiter
            + "data: \"中文321\"" + lineDelimiter
            + "id: 2" + lineDelimiter
            + "retry: 3600" + lineDelimiter
            + "event: test" + lineDelimiter.repeat(trailingDelimiterCount);
    final byte[] messageTemplateBytes = messageTemplate.getBytes(StandardCharsets.UTF_8);

    int[] splitIndexes = new int[splitIndexesCount];
    try {
      runDecodeResponseHalfPackageTemplate(0, splitIndexes, messageTemplateBytes);
    } catch (AssertionError e) {
      throw new AssertionError(e.getMessage() + ", messageTemplate=["
          + messageTemplate
          + "]", e);
    }
  }

  private void runDecodeResponseHalfPackageTemplate(int cursor, int[] splitIndexes, byte[] messageTemplateBytes) {
    if (cursor != splitIndexes.length) {
      for (int i = cursor == 0 ? 0 : splitIndexes[cursor - 1]; i <= messageTemplateBytes.length; ++i) {
        splitIndexes[cursor] = i;
        runDecodeResponseHalfPackageTemplate(cursor + 1, splitIndexes, messageTemplateBytes);
      }
      return;
    }

    final List<ByteArrayInputStream> byteArrayInputStreams = splitByteArrayInputStreams(
        splitIndexes, messageTemplateBytes);

    final ProduceEventStreamProcessor processor = new ProduceEventStreamProcessor();
    final JavaType javaType = RestObjectMapperFactory.getRestObjectMapper().constructType(String.class);

    final List<SseEventResponseEntity<?>> entityList = new ArrayList<>();
    for (ByteArrayInputStream byteArrayInputStream : byteArrayInputStreams) {
      final List<SseEventResponseEntity<?>> entities;
      try {
        entities = (List<SseEventResponseEntity<?>>) processor.decodeResponse(
            byteArrayInputStream, javaType);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      entityList.addAll(entities);
    }
    try {
      entityList.addAll(((Flowable<SseEventResponseEntity<?>>) processor.close()).toList().blockingGet());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    try {
      checkDecodeResult(entityList,
          new ItemChecker(0, Collections.singletonList("中文aaa"), null, null),
          new ItemChecker(1, Arrays.asList("bbb中文", "123汉语"), null, null),
          new ItemChecker(2, Arrays.asList("~!@#$%^&*()_+=-0987654321`中文[]{}\\|;':\",./<>?abc",
              "文字ccc", "中文321"), "test", 3600L));
    } catch (AssertionError e) {
      throw new AssertionError(e.getMessage() + System.lineSeparator() +
          ", splitIndexes=" + Arrays.toString(splitIndexes), e);
    }
  }

  private List<ByteArrayInputStream> splitByteArrayInputStreams(int[] splitIndexes, byte[] messageTemplateBytes) {
    int splitHeader = 0;
    final List<ByteArrayInputStream> byteArrayInputStreams = new ArrayList<>();
    for (int split : splitIndexes) {
      final byte[] segment = Arrays.copyOfRange(messageTemplateBytes, splitHeader, split);
      final ByteArrayInputStream stream = new ByteArrayInputStream(segment);
      byteArrayInputStreams.add(stream);
      splitHeader = split;
    }

    final byte[] segment = Arrays.copyOfRange(messageTemplateBytes, splitIndexes[splitIndexes.length - 1],
        messageTemplateBytes.length);
    final ByteArrayInputStream stream = new ByteArrayInputStream(segment);
    byteArrayInputStreams.add(stream);

    return byteArrayInputStreams;
  }

  private void checkDecodeResult(Object obj) {
    MatcherAssert.assertThat(obj, Matchers.instanceOf(List.class));
    MatcherAssert.assertThat(((List<?>) obj).size(), Matchers.equalTo(0));
  }

  private void checkDecodeResult(Object obj, ItemChecker... checkers) {
    MatcherAssert.assertThat(obj, Matchers.instanceOf(List.class));
    final List<?> objList = (List<?>) obj;
    MatcherAssert.assertThat("expect size of objList is " + checkers.length,
        objList.size(), Matchers.equalTo(checkers.length));

    for (int i = 0; i < objList.size(); i++) {
      Object result = objList.get(i);
      checkers[i]
          .check(result);
    }
  }

  private ByteArrayInputStream prepareStream(String buffer) {
    return new ByteArrayInputStream(
        buffer.getBytes(StandardCharsets.UTF_8));
  }

  private static class ItemChecker {
    private int id;

    private List<String> expectDataList;

    private String event;

    private Long retry;

    public ItemChecker(int id, List<String> expectDataList, String event, Long retry) {
      this.id = id;
      this.expectDataList = expectDataList;
      this.event = event;
      this.retry = retry;
    }

    public void check(Object actualResult) {
      final SseEventResponseEntity<?> entity0 = ((SseEventResponseEntity<?>) actualResult);
      MatcherAssert.assertThat(entity0.getData(), Matchers.instanceOf(List.class));
      final List<?> data = entity0.getData();
      MatcherAssert.assertThat("actual data = " + data.toString(), data.size(),
          Matchers.equalTo(expectDataList.size()));
      for (int i = 0; i < data.size(); i++) {
        MatcherAssert.assertThat(data.get(i), Matchers.equalTo(expectDataList.get(i)));
      }
      MatcherAssert.assertThat("actual event = " + entity0.getEvent(),
          entity0.getEvent(), event == null ? Matchers.nullValue() : Matchers.equalTo(event));
      MatcherAssert.assertThat("actual retry = " + entity0.getRetry(),
          entity0.getRetry(), retry == null ? Matchers.nullValue() : Matchers.equalTo(retry));
      MatcherAssert.assertThat("actual id = " + entity0.getId(),
          entity0.getId(), Matchers.equalTo(id));
    }
  }
}
