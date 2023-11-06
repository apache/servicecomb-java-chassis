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
package org.apache.servicecomb.core.invocation;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;

/**
 * <pre>
 * for consumer:
 *
 *  1. total: start create invocation -> all filters finished
 *  2. prepare: start create invocation -> finish create invocation
 *  3. filters-(filter-name): start call on filter -> on complete
 *  4. connection: start get connection -> finish get connection
 *  5. consumer-encode: start encode request -> finish encode request
 *  6. consumer-decode: start decode response -> finish decode response
 *  7. consumer-send: start send request -> finish send request
 *  8. wait: finish send request -> start decode response
 *
 * for provider:
 *  1. total: start create invocation -> all filters finished
 *  2. prepare: start create invocation -> finish create invocation
 *  3. filters-(filter-name): start call on filter -> on complete
 *  4. queue: add in queue -> execute in thread
 *  5. provider-decode: start decode request -> finish decode request
 *  6. provider-encode: start encode response -> finish encode response
 *  7. provider-send: start send response -> finish send response
 *  8. execute: start business execute -> finish business execute
 *
 * for edge:
 *
 *  *  1. total: start create invocation -> all filters finished
 *  *  2. prepare: start create invocation -> finish create invocation
 *  *  3. filters-(filter-name): start call on filter -> on complete
 *  *  4. connection: start get connection -> finish get connection
 *  *  5. provider-decode: start decode request -> finish decode request
 *  *  6. provider-encode: start encode response -> finish encode response
 *  *  7. consumer-encode: start encode request -> finish encode request
 *  *  8. consumer-decode: start decode response -> finish decode response
 *  *  9. consumer-send: start send request -> finish send request
 *  *  10. provider-send: start send response -> finish send response
 *  *  11. wait: finish send request -> start decode response
 *
 * </pre>
 */
public class InvocationStageTrace {
  public static class Stage {
    private long beginTime;

    private long endTime;

    public long getBeginTime() {
      return beginTime;
    }

    public long getEndTime() {
      return endTime;
    }
  }

  public static final String STAGE_TOTAL = "total";

  public static final String STAGE_PREPARE = "prepare";

  public static final String STAGE_PROVIDER_QUEUE = "queue";

  public static final String STAGE_PROVIDER_DECODE_REQUEST = "provider-decode";

  public static final String STAGE_PROVIDER_ENCODE_RESPONSE = "provider-encode";

  public static final String STAGE_PROVIDER_SEND = "provider-send";

  public static final String STAGE_PROVIDER_BUSINESS = "execute";

  public static final String STAGE_CONSUMER_CONNECTION = "connection";

  public static final String STAGE_CONSUMER_ENCODE_REQUEST = "consumer-encode";

  public static final String STAGE_CONSUMER_DECODE_RESPONSE = "consumer-decode";

  public static final String STAGE_CONSUMER_SEND = "consumer-send";

  public static final String STAGE_CONSUMER_WAIT = "wait";

  private final Invocation invocation;

  // invocation start time in millis, for passing strategy use only
  private long startInMillis;

  private long finish;

  private long startCreateInvocation;

  private long finishCreateInvocation;

  private long startProviderQueue;

  private long finishProviderQueue;

  private long startConsumerConnection;

  private long finishConsumerConnection;

  private long startProviderDecodeRequest;

  private long finishProviderDecodeRequest;

  private long startProviderEncodeResponse;

  private long finishProviderEncodeResponse;

  private long startConsumerEncodeRequest;

  private long finishConsumerEncodeRequest;

  private long startConsumerDecodeResponse;

  private long finishConsumerDecodeResponse;

  private long startProviderSendResponse;

  private long finishProviderSendResponse;

  private long startConsumerSendRequest;

  private long finishConsumerSendRequest;

  private long startBusinessExecute;

  private long finishBusinessExecute;

  private long startWaitResponse;

  private long finishWaitResponse;

  // invocation stage can not be used in concurrent access
  private final Map<String, Stage> stages = new HashMap<>();

  public InvocationStageTrace(Invocation invocation) {
    this.invocation = invocation;
  }

  public String recordStageBegin(String stageName) {
    String realStageName = stageName;
    if (stages.get(stageName) != null) {
      realStageName = realStageName + "@";
    }
    Stage stage = new Stage();
    stage.beginTime = System.nanoTime();
    stages.put(realStageName, stage);
    return realStageName;
  }

  public void recordStageEnd(String realStageName) {
    Stage stage = stages.get(realStageName);
    stage.endTime = nanoTime();
  }

  public Map<String, Stage> getStages() {
    return stages;
  }

  public void finish() {
    this.finish = nanoTime();
  }

  public void startCreateInvocation(long nano) {
    this.startCreateInvocation = nano;
    this.startInMillis = millisTime();
  }

  public void finishCreateInvocation() {
    this.finishCreateInvocation = nanoTime();
  }

  public long calcPrepare() {
    return calc(finishCreateInvocation, startCreateInvocation);
  }

  public void startProviderQueue() {
    this.startProviderQueue = nanoTime();
  }

  public void finishProviderQueue() {
    this.finishProviderQueue = nanoTime();
  }

  public long calcQueue() {
    return calc(finishProviderQueue, startProviderQueue);
  }

  public void startProviderDecodeRequest() {
    this.startProviderDecodeRequest = nanoTime();
  }

  public void finishProviderDecodeRequest() {
    this.finishProviderDecodeRequest = nanoTime();
  }

  public long calcProviderDecodeRequest() {
    return calc(finishProviderDecodeRequest, startProviderDecodeRequest);
  }

  public void startProviderEncodeResponse() {
    this.startProviderEncodeResponse = nanoTime();
  }

  public void finishProviderEncodeResponse() {
    this.finishProviderEncodeResponse = nanoTime();
  }

  public long calcProviderEncodeResponse() {
    return calc(finishProviderEncodeResponse, startProviderEncodeResponse);
  }

  public void startConsumerEncodeRequest() {
    this.startConsumerEncodeRequest = nanoTime();
  }

  public void finishConsumerEncodeRequest() {
    this.finishConsumerEncodeRequest = nanoTime();
  }

  public long calcConsumerEncodeRequest() {
    return calc(finishConsumerEncodeRequest, startConsumerEncodeRequest);
  }

  public void startConsumerDecodeResponse() {
    this.startConsumerDecodeResponse = nanoTime();
  }

  public void finishConsumerDecodeResponse() {
    this.finishConsumerDecodeResponse = nanoTime();
  }

  public long calcConsumerDecodeResponse() {
    return calc(finishConsumerDecodeResponse, startConsumerDecodeResponse);
  }

  public void startProviderSendResponse() {
    this.startProviderSendResponse = nanoTime();
  }

  public void finishProviderSendResponse() {
    this.finishProviderSendResponse = nanoTime();
  }

  public long calcProviderSendResponse() {
    return calc(finishProviderSendResponse, startProviderSendResponse);
  }

  public void startBusinessExecute() {
    this.startBusinessExecute = nanoTime();
  }

  public void finishBusinessExecute() {
    this.finishBusinessExecute = nanoTime();
  }

  public long calcBusinessExecute() {
    return calc(finishBusinessExecute, startBusinessExecute);
  }

  public void startConsumerConnection() {
    this.startConsumerConnection = nanoTime();
  }

  public void finishConsumerConnection() {
    this.finishConsumerConnection = nanoTime();
  }

  public long calcConnection() {
    return calc(finishConsumerConnection, startConsumerConnection);
  }

  public void startConsumerSendRequest() {
    this.startConsumerSendRequest = nanoTime();
  }

  public void finishConsumerSendRequest() {
    this.finishConsumerSendRequest = nanoTime();
  }

  public long calcConsumerSendRequest() {
    return calc(finishConsumerSendRequest, startConsumerSendRequest);
  }

  public void startWaitResponse() {
    this.startWaitResponse = nanoTime();
  }

  public void finishWaitResponse() {
    this.finishWaitResponse = nanoTime();
  }

  public long calcWait() {
    return calc(finishWaitResponse, startWaitResponse);
  }

  public long calcTotal() {
    return calc(finish, this.startCreateInvocation);
  }

  public long getStartInMillis() {
    return this.startInMillis;
  }

  public static long calc(long finish, long start) {
    if (finish == 0 || start == 0) {
      return 0;
    }

    return finish - start;
  }

  /*
   * Holder for testing purpose
   */
  protected long nanoTime() {
    return System.nanoTime();
  }

  protected long millisTime() {
    return System.currentTimeMillis();
  }
}
