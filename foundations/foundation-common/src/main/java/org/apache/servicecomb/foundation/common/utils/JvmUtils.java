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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public final class JvmUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(JvmUtils.class);

  // available for oracle jdk/ open jdk, and maybe others
  @VisibleForTesting
  static final String SUN_JAVA_COMMAND = "sun.java.command";

  private JvmUtils() {
  }

  /**
   *
   * @return main class or null, never throw exception
   */
  public static Class<?> findMainClass() {
    String command = System.getProperty(SUN_JAVA_COMMAND);
    if (command == null || command.isEmpty()) {
      return null;
    }

    // command is main class and args
    String mainClass = command.trim().split(" ")[0];
    try {
      return Class.forName(mainClass);
    } catch (Throwable e) {
      LOGGER.warn("\"{}\" is not a valid class.", mainClass, e);
      return null;
    }
  }
}