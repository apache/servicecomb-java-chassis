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

package org.apache.servicecomb.tests.tracing;

import org.apache.log4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import brave.internal.HexCodec;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;

@Configuration
class TraceContextConfig {

  @Bean
  @Primary
  CurrentTraceContext traceContext() {
    return ParentAwareMDCCurrentTraceContext.create();
  }

  private static class ParentAwareMDCCurrentTraceContext extends CurrentTraceContext {
    @SuppressWarnings("deprecation")
    static ParentAwareMDCCurrentTraceContext create() {
      return new ParentAwareMDCCurrentTraceContext(new Default());
    }

    private final CurrentTraceContext delegate;

    ParentAwareMDCCurrentTraceContext(CurrentTraceContext delegate) {
      if (delegate == null) {
        throw new NullPointerException("delegate == null");
      }
      this.delegate = delegate;
    }

    @Override
    public TraceContext get() {
      return delegate.get();
    }

    @Override
    public Scope newScope(TraceContext currentSpan) {
      final Object previousTraceId = MDC.get("traceId");
      final Object previousSpanId = MDC.get("spanId");
      final Object previousParentId = MDC.get("parentId");

      if (currentSpan != null) {
        MDC.put("traceId", currentSpan.traceIdString());
        MDC.put("spanId", HexCodec.toLowerHex(currentSpan.spanId()));
        if (currentSpan.parentId() != null) {
          MDC.put("parentId", HexCodec.toLowerHex(currentSpan.parentId()));
        }
      } else {
        MDC.remove("traceId");
        MDC.remove("spanId");
        MDC.remove("parentId");
      }

      Scope scope = delegate.newScope(currentSpan);
      class MDCCurrentTraceContextScope implements Scope {
        @Override
        public void close() {
          scope.close();
          if (previousTraceId != null) {
            MDC.put("traceId", previousTraceId);
          } else {
            MDC.remove("traceId");
          }

          if (previousSpanId != null) {
            MDC.put("spanId", previousSpanId);
          } else {
            MDC.remove("spanId");
          }

          if (previousParentId != null) {
            MDC.put("parentId", previousParentId);
          } else {
            MDC.remove("parentId");
          }
        }
      }
      return new MDCCurrentTraceContextScope();
    }
  }
}
