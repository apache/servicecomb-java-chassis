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

package org.apache.servicecomb.serviceregistry.client.http;

import io.vertx.core.buffer.Buffer;

/**
 * To carry the rest response information.
 * @param <T> Type of response body
 */
public class Holder<T> {
  /**
   * The deserialized response body
   */
  T value;

  int statusCode;

  Throwable throwable;

  /**
   * In error situation, response body cannot be deserialized to expected response type,
   * so we record raw body.
   */
  Buffer rawResponseBody;

  /**
   * Copy inner state from another holder object, except for {@link #value}
   * @param fromHolder copy from {@code fromHolder}
   * @return reference of this holder itself
   */
  public Holder<T> copyFrom(Holder<?> fromHolder) {
    this.statusCode = fromHolder.statusCode;
    this.throwable = fromHolder.throwable;
    this.rawResponseBody = fromHolder.rawResponseBody;
    return this;
  }

  public T getValue() {
    return value;
  }

  public Holder<T> setValue(T value) {
    this.value = value;
    return this;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Holder<T> setStatusCode(int statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public Holder<T> setThrowable(Throwable throwable) {
    this.throwable = throwable;
    return this;
  }

  public Buffer getRawResponseBody() {
    return rawResponseBody;
  }

  public Holder<T> setRawResponseBody(Buffer rawResponseBody) {
    this.rawResponseBody = rawResponseBody;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Holder{");
    sb.append("value=").append(value);
    sb.append(", statusCode=").append(statusCode);
    sb.append(", throwable=").append(throwable);
    sb.append(", rawResponseBody=").append(rawResponseBody);
    sb.append('}');
    return sb.toString();
  }
}
