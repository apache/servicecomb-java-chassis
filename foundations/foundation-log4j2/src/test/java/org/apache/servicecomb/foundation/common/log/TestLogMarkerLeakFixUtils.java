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

import org.apache.servicecomb.foundation.common.utils.ReflectUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.IMarkerFactory;
import org.slf4j.impl.StaticMarkerBinder;

import mockit.Expectations;

public class TestLogMarkerLeakFixUtils {
  static IMarkerFactory orgMarkerFactory = StaticMarkerBinder.SINGLETON.getMarkerFactory();

  @After
  public void tearDown() {
    ReflectUtils.setField(StaticMarkerBinder.SINGLETON, "markerFactory", orgMarkerFactory);
  }

  @Test
  public void noBinder() {
    new Expectations(ReflectUtils.class) {
      {
        ReflectUtils.getClassByName("org.slf4j.impl.StaticMarkerBinder");
        result = null;
      }
    };

    // nothing happened
    LogMarkerLeakFixUtils.fix();
  }

  // log4j2 need to adapt slf4j marker to log4j2 marker, and always cache the adapt result
  // need to fix
  @Test
  public void log4j2() {
    Assert.assertEquals("org.apache.logging.slf4j.Log4jMarkerFactory",
        StaticMarkerBinder.SINGLETON.getMarkerFactory().getClass().getName());

    LogMarkerLeakFixUtils.fix();

    Assert.assertEquals(NoCacheLog4jMarkerFactory.class, StaticMarkerBinder.SINGLETON.getMarkerFactory().getClass());
  }
}
