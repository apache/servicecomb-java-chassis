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
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.apache.logging.slf4j.Log4jMarkerFactory;
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
    Class<?> staticMarkerBinderClass = ReflectUtils.getClassByName("org.slf4j.impl.StaticMarkerBinder");
    if (staticMarkerBinderClass != null) {
      IMarkerFactory markerFactory = StaticMarkerBinder.SINGLETON.getMarkerFactory();
      // fix log4j-slf4j-impl (used with SLF$J 1.7.x releases or older)
      IMarkerFactory fixedMarkerFactory = getFixedSlf4jMarkerFactory(markerFactory);
      ReflectUtils.setField(StaticMarkerBinder.SINGLETON, "markerFactory", fixedMarkerFactory);
      LOGGER.info("fixed Log4jMarkerFactory marker leak problem.");
    } else if (LoggerFactory.getILoggerFactory().getClass().getName().equals("org.apache.logging.slf4j.Log4jLoggerFactory")) {
      // fix log4j-slf4j18-impl (used with SLF$J 1.8.x releases or newer)
      Log4jLoggerFactory log4jLoggerFactory = (Log4jLoggerFactory) LoggerFactory.getILoggerFactory();
      try {
        Field markerFactoryField = Log4jLoggerFactory.class.getDeclaredField("markerFactory");
        markerFactoryField.setAccessible(true);
        Log4jMarkerFactory markerFactory = (Log4jMarkerFactory) markerFactoryField.get(log4jLoggerFactory);
        IMarkerFactory fixedMarkerFactory = getFixedSlf4jMarkerFactory(markerFactory);
        ReflectUtils.setField(log4jLoggerFactory, "markerFactory", fixedMarkerFactory);
        LOGGER.info("fixed Log4jMarkerFactory marker leak problem.");
      } catch (NoSuchFieldException | IllegalAccessException e) {
        // if slf4j upgrade again and fixed failed, we should just fail the whole application.
        throw new IllegalStateException("Failed to fix Log4jMarkerFactory leak problem.", e);
      }
    }
  }

  private static IMarkerFactory getFixedSlf4jMarkerFactory(IMarkerFactory markerFactory) {
    if (markerFactory.getClass().getName().equals("org.apache.logging.slf4j.Log4jMarkerFactory")) {
      IMarkerFactory fixedMarkerFactory = getFixedMarkerFactory(markerFactory, new NoCacheLog4jMarkerFactory(),
              "Failed to fix Log4jMarkerFactory leak problem.");
      return fixedMarkerFactory;
    }

    return markerFactory;
  }


  @SuppressWarnings("unchecked")
  private static IMarkerFactory getFixedMarkerFactory(IMarkerFactory orgFactory, NoCacheLog4jMarkerFactory fixedFactory,
                                                      String failMessage) {
    try {
      Field markerField = FieldUtils.getDeclaredField(orgFactory.getClass(), "markerMap", true);
      ConcurrentMap<String, Marker> orgMarkerMap = (ConcurrentMap<String, Marker>) FieldUtils
              .readField(markerField, orgFactory);

      ConcurrentMap<String, Marker> newMarkerMap = (ConcurrentMap<String, Marker>) FieldUtils
              .readField(markerField, fixedFactory);
      newMarkerMap.putAll(orgMarkerMap);
      return fixedFactory;
    } catch (Throwable e) {
      throw new IllegalStateException(failMessage, e);
    }
  }
}
