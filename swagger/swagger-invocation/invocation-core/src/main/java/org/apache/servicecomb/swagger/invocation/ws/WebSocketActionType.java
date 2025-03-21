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

package org.apache.servicecomb.swagger.invocation.ws;

/**
 * The action types performed by {@link WebSocketAdapter} that the users may be aware of.
 */
public enum WebSocketActionType {
  CONNECTION_PREPARE,
  ON_OPEN,
  ON_MESSAGE_TEXT,
  ON_MESSAGE_BINARY,
  ON_FRAME,
  ON_SEND_QUEUE_DRAIN,
  ON_ERROR,
  ON_CLOSE,
  DO_CLOSE,
  DO_PAUSE,
  DO_RESUME,
  DO_SEND_TEXT,
  DO_SEND_BINARY;
}
