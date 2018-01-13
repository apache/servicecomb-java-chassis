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

package org.apache.servicecomb.tracing.zipkin;

import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;

class ZipkinTracingAdviser {

  static final String CALL_PATH = "call.path";

  private final Tracer tracer;

  ZipkinTracingAdviser(Tracer tracer) {
    this.tracer = tracer;
  }

  <T> T invoke(String spanName, String path, ThrowableSupplier<T> supplier) throws Throwable {
    Span span = createSpan(spanName, path);
    try (SpanInScope spanInScope = tracer.withSpanInScope(span)) {
      return supplier.get();
    } catch (Throwable throwable) {
      span.tag("error", throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
      throw throwable;
    } finally {
      span.finish();
    }
  }

  private Span createSpan(String spanName, String path) {
    Span currentSpan = tracer.currentSpan();
    if (currentSpan != null) {
      return tracer.newChild(currentSpan.context()).name(spanName).tag(CALL_PATH, path).start();
    }

    return tracer.newTrace().name(spanName).tag(CALL_PATH, path).start();
  }

  interface ThrowableSupplier<T> {
    T get() throws Throwable;
  }

}
