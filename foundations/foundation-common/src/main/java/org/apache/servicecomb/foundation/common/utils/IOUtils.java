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

import java.io.Closeable;

import org.apache.commons.lang3.StringUtils;

public class IOUtils {
  @SuppressWarnings("deprecation")
  public static void closeQuietly(final Closeable closeable) {
    org.apache.commons.io.IOUtils.closeQuietly(closeable);
  }

  public static String anonymousPath(String path) {
    if (StringUtils.isEmpty(path)) {
      return path;
    }
    StringBuilder result = new StringBuilder();
    char separator = path.contains("/") ? '/' : '\\';
    char[] tokens = path.toCharArray();
    boolean byPass = false;
    boolean fileName = true;
    for (int i = tokens.length - 1; i >= 0; i--) {
      if (tokens[i] == separator) {
        fileName = false;
        byPass = false;
        result.append(separator);
        continue;
      }
      if (!byPass || fileName) {
        result.append(tokens[i]);
        byPass = true;
      }
    }
    return result.reverse().toString();
  }
}
