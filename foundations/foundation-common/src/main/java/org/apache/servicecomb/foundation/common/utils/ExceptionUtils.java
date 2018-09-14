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

package org.apache.servicecomb.foundation.common.utils;

public class ExceptionUtils {
  private static final int DEPTH = 5;

  private ExceptionUtils() {

  }

  public static String getExceptionMessageWithoutTrace(Throwable e) {
    StringBuilder result = new StringBuilder();
    appendExceptionInfo(e, result);
    Throwable cause = e.getCause();
    int depth = DEPTH;
    while (cause != null && depth-- > 0) {
      result.append(";");
      appendExceptionInfo(cause, result);
      cause = cause.getCause();
    }
    return result.toString();
  }

  private static void appendExceptionInfo(Throwable e, StringBuilder result) {
    result.append("cause:");
    result.append(e.getClass().getSimpleName());
    result.append(",message:");
    result.append(e.getMessage());
  }
}
