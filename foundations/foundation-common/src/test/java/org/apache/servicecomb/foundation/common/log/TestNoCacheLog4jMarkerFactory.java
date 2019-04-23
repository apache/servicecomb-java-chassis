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
import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Marker;

public class TestNoCacheLog4jMarkerFactory {
  static class TestNoCacheMarker extends AbstractMarker implements NoCacheMarker {
    private static final long serialVersionUID = -1L;

    @Override
    public String getName() {
      return null;
    }
  }

  static class TestMarker extends AbstractMarker {
    private static final long serialVersionUID = -1L;

    String name;

    @Override
    public String getName() {
      return name;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void noCache() throws IllegalAccessException {
    NoCacheLog4jMarkerFactory markerFactory = new NoCacheLog4jMarkerFactory();
    Field field = FieldUtils.getDeclaredField(Log4jMarkerFactory.class, "markerMap", true);
    ConcurrentMap<String, Marker> markerMap = (ConcurrentMap<String, Marker>) FieldUtils
        .readField(field, markerFactory);

    TestNoCacheMarker marker = new TestNoCacheMarker();
    Marker newMarker = markerFactory.getMarker(marker);

    Assert.assertSame(NoCacheLog4j2Marker.class, newMarker.getClass());
    Assert.assertEquals(0, markerMap.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void cache() throws IllegalAccessException {
    NoCacheLog4jMarkerFactory markerFactory = new NoCacheLog4jMarkerFactory();
    Field field = FieldUtils.getDeclaredField(Log4jMarkerFactory.class, "markerMap", true);
    ConcurrentMap<String, Marker> markerMap = (ConcurrentMap<String, Marker>) FieldUtils
        .readField(field, markerFactory);

    TestMarker marker = new TestMarker();

    {
      Assert.assertEquals(0, markerMap.size());

      marker.name = "1";
      Marker newMarker = markerFactory.getMarker(marker);

      Assert.assertEquals("org.apache.logging.slf4j.Log4jMarker", newMarker.getClass().getName());
      Assert.assertEquals(1, markerMap.size());
    }

    {
      Assert.assertEquals(1, markerMap.size());

      marker.name = "2";
      Marker newMarker = markerFactory.getMarker(marker);

      Assert.assertEquals("org.apache.logging.slf4j.Log4jMarker", newMarker.getClass().getName());
      Assert.assertEquals(2, markerMap.size());
    }
  }
}
