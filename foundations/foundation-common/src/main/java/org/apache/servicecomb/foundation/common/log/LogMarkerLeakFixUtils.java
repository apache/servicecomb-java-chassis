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
package org.apache.servicecomb.foundation.common.log;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.slf4j.IMarkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.impl.StaticMarkerBinder;

/**
 * <pre>
 * ScbMaker.getName() is relate to invocation traceId and invocationId
 * it's dynamic, not static
 *
 * this will cause log4j2 always create new Marker instance and cache them
 * that's a memory leak problem
 * </pre>
 */
public final class LogMarkerLeakFixUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogMarkerLeakFixUtils.class);

  private LogMarkerLeakFixUtils() {
  }

  @SuppressWarnings("unchecked")
  public static void fix() {
    Class<?> staticMarkerBinderClass = ReflectUtils.getClassByName(null, "org.slf4j.impl.StaticMarkerBinder");
    if (staticMarkerBinderClass == null) {
      return;
    }

    IMarkerFactory markerFactory = StaticMarkerBinder.SINGLETON.getMarkerFactory();
    if (markerFactory.getClass().getName()
        .equals("org.apache.servicecomb.foundation.common.log.NoCacheLog4jMarkerFactory")) {
      return;
    }

    if (markerFactory.getClass().getName().equals("org.apache.logging.slf4j.Log4jMarkerFactory")) {
      fixMarkerFactory(markerFactory, new NoCacheLog4jMarkerFactory(),
          "Failed to fix Log4jMarkerFactory leak problem.");
      LOGGER.info("fixed Log4jMarkerFactory marker leak problem.");
      return;
    }
  }

  @SuppressWarnings("unchecked")
  private static void fixMarkerFactory(IMarkerFactory orgFactory, NoCacheLog4jMarkerFactory fixedFactory,
      String failMessage) {
    try {
      Field markerField = FieldUtils.getDeclaredField(orgFactory.getClass(), "markerMap", true);
      ConcurrentMap<String, Marker> orgMarkerMap = (ConcurrentMap<String, Marker>) FieldUtils
          .readField(markerField, orgFactory);

      ConcurrentMap<String, Marker> newMarkerMap = (ConcurrentMap<String, Marker>) FieldUtils
          .readField(markerField, fixedFactory);
      newMarkerMap.putAll(orgMarkerMap);
      ReflectUtils.setField(StaticMarkerBinder.SINGLETON, "markerFactory", fixedFactory);
    } catch (Throwable e) {
      throw new IllegalStateException(failMessage, e);
    }
  }
}
