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
package org.apache.servicecomb.transport.rest.client;

import java.util.Map;

import javax.servlet.http.Part;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;

import com.google.common.collect.Multimap;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;

public interface RestClientRequestParameters extends RestClientRequest {
  Map<String, String> getCookieMap();

  Map<String, Object> getFormMap();

  Multimap<String, Part> getUploads();

  @Override
  Buffer getBodyBuffer();

  void setBodyBuffer(Buffer bodyBuffer);

  @Override
  default void write(Buffer bodyBuffer) {
    setBodyBuffer(bodyBuffer);
  }

  @Override
  default Future<Void> end() {
    throw new UnsupportedOperationException("should not invoke this method");
  }
}
