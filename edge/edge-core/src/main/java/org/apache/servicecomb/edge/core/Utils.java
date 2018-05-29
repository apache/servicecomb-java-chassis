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

package org.apache.servicecomb.edge.core;

/**
 * Commonly used methods in this package.
 */
public final class Utils {
  private Utils() {

  }

  /**
   * Get the actual path without prefix
   * @param path full path
   * @param pathIndex the index of / that after prefix
   * @return actual path
   */
  public static String findActualPath(String path, int pathIndex) {
    if (pathIndex <= 0) {
      return path;
    }

    int fromIndex = 0;
    int counter = pathIndex;
    char[] chars = path.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == '/') {
        if (--counter < 0) {
          fromIndex = i;
          break;
        }
      }
    }
    return fromIndex > 0 ? path.substring(fromIndex) : "";
  }
}
