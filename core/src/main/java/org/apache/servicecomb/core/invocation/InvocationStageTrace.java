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

import org.apache.servicecomb.core.Invocation;

/**
 * <pre>
 * important:
 *   all time point is about invocation stage, not java method
 *   all time is nanoTime
 *   not all stage relate to a event, currently, we only have 4 event:
 *     start/finish/startBusiness/finishBusiness
 *
 * for consumer:
 *         (prepare)                      (handlerReq)                                       (clientFilterReq)
 *   start --------> startHandlersRequest -----------> startClientFiltersRequest --------------------------------------
 *                                                        <----------------(sendRequest)----------------->            |
 *                        (receiveResponse)              (writeToBuffer)                   (getConnection)            |
 *    ---finishReceiveResponse <----- finishWriteToBuffer <----------- finishGetConnection <-------------- startSend <-
 *   | (wakeConsumer)            (clientFiltersResponse)               (handlersResponse)
 *   |-----> startClientFiltersResponse -------> finishClientFiltersResponse -------> finishHandlersResponse --> finish
 *
 * for producer:
 *       (prepare)       (threadPoolQueue)                                (serverFiltersRequest)
 *   start ----> startSchedule -----> startExecution -> startServerFiltersRequest -------> startHandlersRequest -------
 *                          (handlersResponse)          <-------------(business)------------->      (handlersRequest) |
 *   -----finishHandlersResponse <------ finishBusiness <------- finishBusinessMethod <------ startBusinessMethod------
 *   | (serverFiltersResponse)     (sendResponse)
 *   |---> finishServerFiltersResponse ------> finish
 *
 * for edge:
 *      (prepare)         (threadPoolQueue)                               (serverFiltersRequest)
 *   start ----> startSchedule -----> startExecution -> startServerFiltersRequest ----> startHandlersRequest ----------
 *                           <----------(sendRequest)---------->                                                      |
 *                       (writeToBuffer)              (getConnection)  (clientFilterReq)            (handlersRequest) |
 *   --- finishWriteToBuffer <------ finishGetConnection <------ startSend <------ startClientFiltersRequest <---------
 *   | (receiveResponse)     (wakeConsumer)                 (clientFiltersResponse)
 *   ---> finishReceiveResponse ------> startClientFiltersResponse ------> finishClientFiltersResponse ----------------
 *                                   (sendResponse)                  (serverFiltersResponse)       (handlersResponse) |
 *                              finish <------ finishServerFiltersResponse <------ finishHandlersResponse <------------
 *
 * </pre>
 */
public class InvocationStageTrace {
  public static final String PREPARE = "prepare";

  public static final String HANDLERS_REQUEST = "handlers request";

  public static final String HANDLERS_RESPONSE = "handlers response";

  public static final String CLIENT_FILTERS_REQUEST = "client filters request";

  public static final String CONSUMER_SEND_REQUEST = "send request";

  public static final String CONSUMER_GET_CONNECTION = "get connection";

  public static final String CONSUMER_WRITE_TO_BUF = "write to buf";

  public static final String CONSUMER_WAIT_RESPONSE = "wait response";

  public static final String CONSUMER_WAKE_CONSUMER = "wake consumer";

  public static final String CLIENT_FILTERS_RESPONSE = "client filters response";

  public static final String THREAD_POOL_QUEUE = "threadPoolQueue";

  public static final String SERVER_FILTERS_REQUEST = "server filters request";

  public static final String SERVER_FILTERS_RESPONSE = "server filters response";

  public static final String PRODUCER_SEND_RESPONSE = "send response";

  private Invocation invocation;

  private long start;

  private long startHandlersRequest;

  private long startClientFiltersRequest;

  // only for consumer
  private long startSend;

  // only for consumer
  private long finishGetConnection;

  // only for consumer
  private long finishWriteToBuffer;

  // only for consumer
  private long finishReceiveResponse;

  private long startClientFiltersResponse;

  private long finishClientFiltersResponse;

  private long finishHandlersResponse;

  private long finish;

  // only for producer: put producer task to thread pool
  private long startSchedule;

  private long startServerFiltersRequest;

  private long finishServerFiltersResponse;

  // only for producer: start execute in work thread
  //           for reactive mode, work thread is eventloop
  private long startExecution;

  // only for producer
  private long startBusinessMethod;

  // only for producer
  private long finishBusiness;

  public InvocationStageTrace(Invocation invocation) {
    this.invocation = invocation;
  }

  public void start(long start) {
    this.start = start;
  }

  public long getStart() {
    return start;
  }

  public long getStartHandlersRequest() {
    return startHandlersRequest;
  }

  public void startHandlersRequest() {
    this.startHandlersRequest = System.nanoTime();
  }

  public long getStartClientFiltersRequest() {
    return startClientFiltersRequest;
  }

  public void startClientFiltersRequest() {
    this.startClientFiltersRequest = System.nanoTime();
  }

  public long getStartSchedule() {
    return startSchedule;
  }

  public void startSchedule() {
    this.startSchedule = System.nanoTime();
  }

  public long getStartExecution() {
    return startExecution;
  }

  public void startExecution() {
    this.startExecution = System.nanoTime();
  }

  public long getStartSend() {
    return startSend;
  }

  public void startSend() {
    this.startSend = System.nanoTime();
  }

  public long getFinishGetConnection() {
    return finishGetConnection;
  }

  public void finishGetConnection(long finishGetConnection) {
    this.finishGetConnection = finishGetConnection;
  }

  public long getFinishWriteToBuffer() {
    return finishWriteToBuffer;
  }

  public void finishWriteToBuffer(long finishWriteToBuffer) {
    this.finishWriteToBuffer = finishWriteToBuffer;
  }

  public long getFinishReceiveResponse() {
    return finishReceiveResponse;
  }

  public void finishReceiveResponse() {
    this.finishReceiveResponse = System.nanoTime();
  }

  public long getStartClientFiltersResponse() {
    return startClientFiltersResponse;
  }

  public void startClientFiltersResponse() {
    this.startClientFiltersResponse = System.nanoTime();
  }

  public long getFinishClientFiltersResponse() {
    return finishClientFiltersResponse;
  }

  public void finishClientFiltersResponse() {
    this.finishClientFiltersResponse = System.nanoTime();
  }

  public long getFinishHandlersResponse() {
    return finishHandlersResponse;
  }

  public void finishHandlersResponse() {
    this.finishHandlersResponse = System.nanoTime();
  }

  public long getStartServerFiltersRequest() {
    return startServerFiltersRequest;
  }

  public void startServerFiltersRequest() {
    this.startServerFiltersRequest = System.nanoTime();
  }

  public long getFinishServerFiltersResponse() {
    return finishServerFiltersResponse;
  }

  public void finishServerFiltersResponse() {
    this.finishServerFiltersResponse = System.nanoTime();
  }

  public long getStartBusinessMethod() {
    return startBusinessMethod;
  }

  public void startBusinessMethod() {
    this.startBusinessMethod = System.nanoTime();
  }

  public long getFinishBusiness() {
    return finishBusiness;
  }

  public void finishBusiness() {
    this.finishBusiness = System.nanoTime();
  }

  public long getFinish() {
    return finish;
  }

  public void finish() {
    this.finish = System.nanoTime();
  }

  private double calc(long finish, long start) {
    if (finish == 0 || start == 0) {
      return Double.NaN;
    }

    return finish - start;
  }

  public double calcTotalTime() {
    return calc(finish, start);
  }

  public double calcInvocationPrepareTime() {
    if (invocation.isConsumer() && !invocation.isEdge()) {
      return calc(startHandlersRequest, start);
    }

    return calc(startSchedule, start);
  }

  public double calcHandlersRequestTime() {
    if (invocation.isConsumer()) {
      return calc(startClientFiltersRequest, startHandlersRequest);
    }

    return calc(startBusinessMethod, startHandlersRequest);
  }

  public double calcClientFiltersRequestTime() {
    return calc(startSend, startClientFiltersRequest);
  }

  public double calcServerFiltersRequestTime() {
    return calc(startHandlersRequest, startServerFiltersRequest);
  }

  public double calcSendRequestTime() {
    return calc(finishWriteToBuffer, startSend);
  }

  public double calcGetConnectionTime() {
    return calc(finishGetConnection, startSend);
  }

  public double calcWriteToBufferTime() {
    return calc(finishWriteToBuffer, finishGetConnection);
  }

  public double calcReceiveResponseTime() {
    return calc(finishReceiveResponse, finishWriteToBuffer);
  }

  public double calcWakeConsumer() {
    return calc(startClientFiltersResponse, finishReceiveResponse);
  }

  public double calcClientFiltersResponseTime() {
    return calc(finishClientFiltersResponse, startClientFiltersResponse);
  }

  public double calcServerFiltersResponseTime() {
    return calc(finishServerFiltersResponse, finishHandlersResponse);
  }

  public double calcHandlersResponseTime() {
    if (invocation.isConsumer()) {
      return calc(finishHandlersResponse, finishClientFiltersResponse);
    }

    return calc(finishHandlersResponse, finishBusiness);
  }

  public double calcThreadPoolQueueTime() {
    return calc(startExecution, startSchedule);
  }

  public double calcBusinessTime() {
    return calc(finishBusiness, startBusinessMethod);
  }

  public double calcSendResponseTime() {
    return calc(finish, finishServerFiltersResponse);
  }
}
