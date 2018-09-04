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

    public SCBEngine getScbEngine() {
      return scbEngine;
    }

    public void setScbEngine(SCBEngine scbEngine) {
      this.scbEngine = scbEngine;
    }

    public EventType getEventType() {
      return eventType;
    }

    public void setEventType(EventType eventType) {
      this.eventType = eventType;
    }
  }

  default int getOrder() {
    return 0;
  }

  void onBootEvent(BootEvent event);
}
