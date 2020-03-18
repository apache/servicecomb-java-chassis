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
package org.apache.servicecomb.core.tracing;

import org.apache.servicecomb.core.Invocation;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.Marker;

public class TraceIdLogger {
  private static final Marker MARKER = new ScbMarker();

  private static final String KEY_TRACE_ID = "SERVICECOMB_TRACE_ID";

  private final Invocation invocation;

  private String name;

  public TraceIdLogger(Invocation invocation) {
    this.invocation = invocation;
  }

  public Invocation getInvocation() {
    return invocation;
  }

  public final String getName() {
    if (name == null) {
      name = invocation.getTraceId() + "-" + invocation.getInvocationId();
    }
    return name;
  }

  public void error(Logger logger, String format, Object... arguments) {
    MDC.put(KEY_TRACE_ID, getName());
    logger.error(MARKER, format, arguments);
    MDC.remove(KEY_TRACE_ID);
  }

  public void warn(Logger logger, String format, Object... arguments) {
    MDC.put(KEY_TRACE_ID, getName());
    logger.warn(MARKER, format, arguments);
    MDC.remove(KEY_TRACE_ID);
  }

  public void info(Logger logger, String format, Object... arguments) {
    MDC.put(KEY_TRACE_ID, getName());
    logger.info(MARKER, format, arguments);
    MDC.remove(KEY_TRACE_ID);
  }

  public void debug(Logger logger, String format, Object... arguments) {
    MDC.put(KEY_TRACE_ID, getName());
    logger.debug(MARKER, format, arguments);
    MDC.remove(KEY_TRACE_ID);
  }
}
