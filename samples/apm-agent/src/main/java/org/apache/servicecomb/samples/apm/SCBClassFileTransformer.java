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
package org.apache.servicecomb.samples.apm;

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SCBClassFileTransformer implements ClassFileTransformer {
  private static final Logger LOGGER = Logger.getLogger(SCBClassFileTransformer.class.getName());

  private volatile boolean loaded;

  private URL scbSpiJar;

  public SCBClassFileTransformer(URL scbSpiJar) {
    this.scbSpiJar = scbSpiJar;
  }

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) {
    if (!loaded && className.startsWith("org/apache/servicecomb")) {
      injectSPI(loader);
    }
    return classfileBuffer;
  }

  private synchronized void injectSPI(ClassLoader loader) {
    if (loaded) {
      return;
    }

    try {
      Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      addURLMethod.setAccessible(true);
      addURLMethod.invoke(loader, scbSpiJar);
      loaded = true;
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE,
          String.format("Failed to inject %s to classLoader %s.", scbSpiJar, loader.getClass().getName()), e);
    }
  }
}
