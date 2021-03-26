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

package org.apache.servicecomb.core;

public interface BootListener {
  enum EventType {
    BEFORE_HANDLER,
    AFTER_HANDLER,
    BEFORE_FILTER,
    AFTER_FILTER,
    BEFORE_PRODUCER_PROVIDER,
    AFTER_PRODUCER_PROVIDER,
    BEFORE_CONSUMER_PROVIDER,
    AFTER_CONSUMER_PROVIDER,
    BEFORE_TRANSPORT,
    AFTER_TRANSPORT,
    BEFORE_REGISTRY,
    AFTER_REGISTRY,
    BEFORE_CLOSE,
    AFTER_CLOSE
  }

  class BootEvent {
    private SCBEngine scbEngine;

    private EventType eventType;

    public BootEvent() {
    }

    public BootEvent(SCBEngine scbEngine, EventType eventType) {
      this.scbEngine = scbEngine;
      this.eventType = eventType;
    }

    public SCBEngine getScbEngine() {
      return scbEngine;
    }

    public BootEvent setScbEngine(SCBEngine scbEngine) {
      this.scbEngine = scbEngine;
      return this;
    }

    public EventType getEventType() {
      return eventType;
    }

    public BootEvent setEventType(EventType eventType) {
      this.eventType = eventType;
      return this;
    }
  }

  default int getOrder() {
    return 0;
  }

  default void onBootEvent(BootEvent event) {
    switch (event.eventType) {
      case BEFORE_HANDLER:
        onBeforeHandler(event);
        return;
      case AFTER_HANDLER:
        onAfterHandler(event);
        return;
      case BEFORE_FILTER:
        onBeforeFilter(event);
        return;
      case AFTER_FILTER:
        onAfterFilter(event);
        return;
      case BEFORE_PRODUCER_PROVIDER:
        onBeforeProducerProvider(event);
        return;
      case AFTER_PRODUCER_PROVIDER:
        onAfterProducerProvider(event);
        return;
      case BEFORE_CONSUMER_PROVIDER:
        onBeforeConsumerProvider(event);
        return;
      case AFTER_CONSUMER_PROVIDER:
        onAfterConsumerProvider(event);
        return;
      case BEFORE_TRANSPORT:
        onBeforeTransport(event);
        return;
      case AFTER_TRANSPORT:
        onAfterTransport(event);
        return;
      case BEFORE_REGISTRY:
        onBeforeRegistry(event);
        return;
      case AFTER_REGISTRY:
        onAfterRegistry(event);
        return;
      case BEFORE_CLOSE:
        onBeforeClose(event);
        return;
      case AFTER_CLOSE:
        onAfterClose(event);
        return;
      default:
        throw new IllegalStateException("unknown boot event type: " + event.eventType);
    }
  }

  default void onBeforeHandler(BootEvent event) {

  }

  default void onAfterHandler(BootEvent event) {

  }

  default void onBeforeFilter(BootEvent event) {

  }

  default void onAfterFilter(BootEvent event) {

  }

  default void onBeforeProducerProvider(BootEvent event) {

  }

  default void onAfterProducerProvider(BootEvent event) {

  }

  default void onBeforeConsumerProvider(BootEvent event) {

  }

  default void onAfterConsumerProvider(BootEvent event) {

  }

  default void onBeforeTransport(BootEvent event) {

  }

  default void onAfterTransport(BootEvent event) {

  }

  default void onBeforeRegistry(BootEvent event) {

  }

  default void onAfterRegistry(BootEvent event) {

  }

  default void onBeforeClose(BootEvent event) {

  }

  default void onAfterClose(BootEvent event) {

  }
}
