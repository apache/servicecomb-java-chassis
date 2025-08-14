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

package org.apache.servicecomb.swagger.invocation.sse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;

public class SseEventResponseEntity<T> {
  private static final Logger LOGGER = LoggerFactory.getLogger(SseEventResponseEntity.class);

  /**
   * event id
   */
  private Integer id;

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
  private T data;

  public SseEventResponseEntity<T> id(int id) {
    if (this.id != null) {
      LOGGER.warn("origin id: [{}] is exists, overridden by the current value: [{}]", this.id, id);
    }
    this.id = id;
    return this;
  }

  public SseEventResponseEntity<T> event(String event) {
    if (!StringUtils.isEmpty(this.event)) {
      LOGGER.warn("origin event: [{}] is exists, overridden by the current value: [{}]", this.event, event);
    }
    this.event = event;
    return this;
  }

  public SseEventResponseEntity<T> retry(long retry) {
    if (this.retry != null) {
      LOGGER.warn("origin retry: [{}] is exists, overridden by the current value: [{}]", this.retry, retry);
    }
    this.retry = retry;
    return this;
  }

  public SseEventResponseEntity<T> data(Object data) {
    if (this.data != null) {
      LOGGER.warn("origin data: [{}] is exists, overridden by the current value: [{}]", this.data, data);
    }
    this.data = (T) data;
    return this;
  }

  public Integer getId() {
    return id;
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
