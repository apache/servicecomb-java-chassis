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
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

public class TraceIdLogger {
  private static final Logger LOGGER = LoggerFactory.getLogger("scb-trace");

  private static final Marker MARKER = new ScbMarker();

  public static final String KEY_TRACE_ID = "SERVICECOMB_TRACE_ID";

  private final Invocation invocation;

  public TraceIdLogger(Invocation invocation) {
    this.invocation = invocation;
  }

  public Invocation getInvocation() {
    return invocation;
  }

  public static String constructSource(String source) {
    return "[" + source + "(" +
        Thread.currentThread().getStackTrace()[2].getLineNumber() + ")]";
  }

  public final String getName() {
    return invocation.getTraceId();
  }

  public void error(String format, Object... arguments) {
    MDC.put(KEY_TRACE_ID, getName());
    LOGGER.error(MARKER, format, arguments);
    MDC.remove(KEY_TRACE_ID);
  }

  public void warn(String format, Object... arguments) {
    MDC.put(KEY_TRACE_ID, getName());
    LOGGER.warn(MARKER, format, arguments);
    MDC.remove(KEY_TRACE_ID);
  }

  public void info(String format, Object... arguments) {
    MDC.put(KEY_TRACE_ID, getName());
    LOGGER.info(MARKER, format, arguments);
    MDC.remove(KEY_TRACE_ID);
  }

  public void debug(String format, Object... arguments) {
    MDC.put(KEY_TRACE_ID, getName());
    LOGGER.debug(MARKER, format, arguments);
    MDC.remove(KEY_TRACE_ID);
  }
}
