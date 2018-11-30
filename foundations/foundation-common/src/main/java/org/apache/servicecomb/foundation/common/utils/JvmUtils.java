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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
    // 1.run with java -cp ......
    //   command is main class and args
    // 2.run with java -jar ......
    //   command is jar file name and args
    String command = System.getProperty(SUN_JAVA_COMMAND);
    if (command == null || command.isEmpty()) {
      return null;
    }

    String mainClassOrJar = command.trim().split(" ")[0];
    String mainClass = readFromJar(mainClassOrJar);
    if (mainClass == null || mainClass.isEmpty()) {
      return null;
    }

    try {
      Class<?> cls = Class.forName(mainClass);
      LOGGER.info("Found main class \"{}\".", mainClass);
      return cls;
    } catch (Throwable e) {
      LOGGER.warn("\"{}\" is not a valid class.", mainClass, e);
      return null;
    }
  }

  private static String readFromJar(String mainClassOrJar) {
    if (!mainClassOrJar.endsWith(".jar")) {
      return mainClassOrJar;
    }

    String manifestUri = "jar:file:" + new File(mainClassOrJar).getAbsolutePath() + "!/" + JarFile.MANIFEST_NAME;

    try {
      URL url = new URL(manifestUri);
      try (InputStream inputStream = url.openStream()) {
        Manifest manifest = new Manifest(inputStream);
        return manifest.getMainAttributes().getValue("Main-Class");
      }
    } catch (Throwable e) {
      LOGGER.warn("Failed to read Main-Class from \"{}\".", manifestUri, e);
      return null;
    }
  }

  /**
   * find a property class loader to avoid null
   */
  public static ClassLoader correctClassLoader(ClassLoader classLoader) {
    ClassLoader targetClassLoader = classLoader;
    if (targetClassLoader == null) {
      targetClassLoader = Thread.currentThread().getContextClassLoader();
    }
    if (targetClassLoader == null) {
      targetClassLoader = JvmUtils.class.getClassLoader();
    }
    return targetClassLoader;
  }

  public static ClassLoader findClassLoader() {
    return correctClassLoader(null);
  }
}