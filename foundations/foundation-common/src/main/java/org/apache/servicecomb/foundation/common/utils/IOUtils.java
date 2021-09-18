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

  public static String convertString(String origin) {
    return convertString(origin,System.getProperty("file.separator").charAt(0));
  }

  public static String convertString(String origin, char separator) {
    if (StringUtils.isEmpty(origin)) {
      return "";
    }
    StringBuilder stringBuilder = new StringBuilder();
    char[] chars = origin.toCharArray();
    boolean byPass = true;
    for (int i = chars.length - 1; i >= 0; i--) {
      if (chars[i] == separator) {
        stringBuilder.insert(0, byPass ? "" : separator);
        stringBuilder.insert(0, byPass ? "" : chars[i + 1]);
        byPass = false;
        continue;
      }
      if (byPass) {
        stringBuilder.insert(0, chars[i]);
      }
    }
    return stringBuilder.toString();
  }

}
