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

package org.apache.servicecomb.core.event;

import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.ws.WebSocket;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketActionType;
import org.apache.servicecomb.swagger.invocation.ws.WebSocketMessage;

/**
 * A websocket action means any notification including websocket message/frame
 * that the underlying framework passed to the user extended "WebSocket handler methods" to handle.
 * And the "WebSocket handler methods" means the extensible subscriber methods defined in the
 * {@link WebSocket}, like {@link WebSocket#onOpen()} and {@link WebSocket#onMessage(WebSocketMessage)}.
 */
public class WebSocketActionEvent {
  /**
   * To indicates whether this websocket connection is on consumer side or producer side.
   */
  private InvocationType invocationType;

  private OperationMeta operationMeta;

  private String traceId;

  private String connectionId;

  /**
   * See {@link WebSocketActionType}.
   */
  private WebSocketActionType actionType;

  /**
   * The startup time of this WebSocket connection in the UnixTimestamp format.
   * We treat the websocket handshaking success time as startup time.
   */
  private long connectionStartTimestamp;

  /**
   * The timestamp that the action is scheduled to run in executor.
   */
  private long scheduleStartTimestamp;

  /**
   * The UnixTimestamp when this websocket action is triggered.
   */
  private long actionStartTimestamp;

  private long actionEndTimestamp;

  private String handleThreadName;

  /**
   * How many bytes of data are passed to the handle methods to handle.
   * Note that some kinds of actions carry no data, like {@link WebSocket#onOpen()}, in which case the dataSize is 0.
   */
  private long dataSize;

  public InvocationType getInvocationType() {
    return invocationType;
  }

  public WebSocketActionEvent setInvocationType(InvocationType invocationType) {
    this.invocationType = invocationType;
    return this;
  }

  public OperationMeta getOperationMeta() {
    return operationMeta;
  }

  public WebSocketActionEvent setOperationMeta(OperationMeta operationMeta) {
    this.operationMeta = operationMeta;
    return this;
  }

  public String getTraceId() {
    return traceId;
  }

  public WebSocketActionEvent setTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  public String getConnectionId() {
    return connectionId;
  }

  public WebSocketActionEvent setConnectionId(String connectionId) {
    this.connectionId = connectionId;
    return this;
  }

  public WebSocketActionType getActionType() {
    return actionType;
  }

  public WebSocketActionEvent setActionType(WebSocketActionType actionType) {
    this.actionType = actionType;
    return this;
  }

  public long getConnectionStartTimestamp() {
    return connectionStartTimestamp;
  }

  public WebSocketActionEvent setConnectionStartTimestamp(long connectionStartTimestamp) {
    this.connectionStartTimestamp = connectionStartTimestamp;
    return this;
  }

  public long getScheduleStartTimestamp() {
    return scheduleStartTimestamp;
  }

  public WebSocketActionEvent setScheduleStartTimestamp(long scheduleStartTimestamp) {
    this.scheduleStartTimestamp = scheduleStartTimestamp;
    return this;
  }

  public long getActionStartTimestamp() {
    return actionStartTimestamp;
  }

  public WebSocketActionEvent setActionStartTimestamp(long actionStartTimestamp) {
    this.actionStartTimestamp = actionStartTimestamp;
    return this;
  }

  public long getActionEndTimestamp() {
    return actionEndTimestamp;
  }

  public WebSocketActionEvent setActionEndTimestamp(long actionEndTimestamp) {
    this.actionEndTimestamp = actionEndTimestamp;
    return this;
  }

  public String getHandleThreadName() {
    return handleThreadName;
  }

  public WebSocketActionEvent setHandleThreadName(String handleThreadName) {
    this.handleThreadName = handleThreadName;
    return this;
  }

  public long getDataSize() {
    return dataSize;
  }

  public WebSocketActionEvent setDataSize(long dataSize) {
    this.dataSize = dataSize;
    return this;
  }
}
