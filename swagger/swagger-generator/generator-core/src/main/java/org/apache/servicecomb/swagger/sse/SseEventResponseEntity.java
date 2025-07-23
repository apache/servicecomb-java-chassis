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

package org.apache.servicecomb.swagger.sse;

import jakarta.validation.constraints.NotNull;

public class SseEventResponseEntity<T> {
  /**
   * event id
   */
  private Integer eventId;

  /**
   * event type
   */
  private String event;

  /**
   * reconnection time
   */
  private Long retry;

  /**
   * business data
   */
  @NotNull
  private Object data;

  public SseEventResponseEntity<T> eventId(int eventId) {
    this.eventId = eventId;
    return this;
  }

  public SseEventResponseEntity<T> event(String event) {
    this.event = event;
    return this;
  }

  public SseEventResponseEntity<T> retry(long retry) {
    this.retry = retry;
    return this;
  }

  public SseEventResponseEntity<T> data(T data) throws Exception {
    this.data = data;
    return this;
  }

  public Integer getEventId() {
    return eventId;
  }

  public String getEvent() {
    return event;
  }

  public Long getRetry() {
    return retry;
  }

  public Object getData() {
    return data;
  }
}
