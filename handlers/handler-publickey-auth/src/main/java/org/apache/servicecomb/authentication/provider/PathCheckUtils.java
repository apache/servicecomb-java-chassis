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

package org.apache.servicecomb.authentication.provider;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

public class PathCheckUtils {
  private static final String KEY_INCLUDE_PATH = "servicecomb.publicKey.accessControl.includePathPatterns";

  private static final String KEY_EXCLUDE_PATH = "servicecomb.publicKey.accessControl.excludePathPatterns";

  /**
   * first determine configured non-authentication path is matched requestPath, if match not needed auth.
   * second determine whether of configured authentication path, if not configured, default all path need auth;
   * if configured, then check whether of matched requestPath, if match needed auth, otherwise not needed auth.
   *
   * @param requestPath path
   * @param env environment
   * @return notRequiredAuth
   */
  public static boolean isNotRequiredAuth(String requestPath, Environment env) {
    if (excludePathMatchPath(requestPath, env)) {
      return true;
    }
    return includePathMatchPath(requestPath, env);
  }

  private static boolean excludePathMatchPath(String requestPath, Environment env) {
    String excludePathPattern = env.getProperty(KEY_EXCLUDE_PATH, "");
    if (StringUtils.isEmpty(excludePathPattern)) {
      return false;
    }
    return isPathMather(requestPath, excludePathPattern);
  }

  private static boolean includePathMatchPath(String requestPath, Environment env) {
    String includePathPattern = env.getProperty(KEY_INCLUDE_PATH, "");
    if (StringUtils.isEmpty(includePathPattern)) {
      return false;
    }
    return !isPathMather(requestPath, includePathPattern);
  }

  private static boolean isPathMather(String requestPath, String pathPattern) {
    for (String pattern : pathPattern.split(",")) {
      if (!pattern.isEmpty() && isPatternMatch(requestPath, pattern)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPatternMatch(String value, String pattern) {
    if (pattern.startsWith("*") || pattern.startsWith("/*")) {
      int index = 0;
      for (int i = 0; i < pattern.length(); i++) {
        if (pattern.charAt(i) != '*' && pattern.charAt(i) != '/') {
          break;
        }
        index++;
      }
      return value.endsWith(pattern.substring(index));
    }
    if (pattern.endsWith("*")) {
      int index = pattern.length() - 1;
      for (int i = pattern.length() - 1; i >= 0; i--) {
        if (pattern.charAt(i) != '*' && pattern.charAt(i) != '/') {
          break;
        }
        index--;
      }
      return value.startsWith(pattern.substring(0, index));
    }
    return value.equals(pattern);
  }
}
