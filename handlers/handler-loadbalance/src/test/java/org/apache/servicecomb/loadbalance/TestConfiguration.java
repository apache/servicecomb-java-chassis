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

package org.apache.servicecomb.loadbalance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import mockit.Mock;
import mockit.MockUp;

/**
 *
 */
public class TestConfiguration {

  @Test
  public void testConstants() {

    assertEquals("servicecomb.loadbalance.", Configuration.PROP_ROOT);
    assertEquals("ribbon.", Configuration.PROP_ROOT_20);
    assertEquals("retryEnabled", Configuration.PROP_RETRY_ENABLED);
    assertEquals("retryOnNext", Configuration.PROP_RETRY_ONNEXT);
    assertEquals("retryOnSame", Configuration.PROP_RETRY_ONSAME);
    assertEquals("SessionStickinessRule.successiveFailedTimes", Configuration.SUCCESSIVE_FAILED_TIMES);

    assertNotNull(Configuration.INSTANCE);
  }

  @Test
  public void testFullConfigurationWithArgsString() {
    assertNotNull(Configuration.INSTANCE.getRetryOnNext("test"));
    assertNotNull(Configuration.INSTANCE.getRetryOnSame("test"));
    assertNotNull(Configuration.INSTANCE.isRetryEnabled("test"));
    assertNotNull(Configuration.INSTANCE.getSuccessiveFailedTimes("test"));
    assertNotNull(Configuration.INSTANCE.getSessionTimeoutInSeconds("test"));
  }

  @Test
  public void testConfigurationWithGetpropertyReturnsStringChar() {

    new MockUp<Configuration>() {
      @Mock
      private String getProperty(String defaultValue, String... keys) {
        return "tyt";
      }
    };

    Configuration.INSTANCE.getRetryOnNext("test");

    assertNotNull(Configuration.INSTANCE.getRetryOnNext("test"));
  }

  @Test
  public void testConfigurationWithGetpropertyReturnsStringNum() {

    new MockUp<Configuration>() {

      @Mock
      private String getProperty(String defaultValue, String... keys) {
        return "1234";
      }
    };

    Configuration.INSTANCE.getRetryOnNext("test");

    assertNotNull(Configuration.INSTANCE.getRetryOnNext("test"));
  }

  @Test
  public void testGetRetryOnSameWithGetpropertyReturnsStringChar() {

    new MockUp<Configuration>() {
      @Mock
      private String getProperty(String defaultValue, String... keys) {
        return "tyt";
      }
    };

    Configuration.INSTANCE.getRetryOnSame("test");
    assertNotNull(Configuration.INSTANCE.getRetryOnSame("test"));
  }

  @Test
  public void testGetRetryOnSameWithGetpropertyReturnsStringNum() {

    new MockUp<Configuration>() {

      @Mock
      private String getProperty(String defaultValue, String... keys) {
        return "1234";
      }
    };

    Configuration.INSTANCE.getRetryOnSame("test");
    assertNotNull(Configuration.INSTANCE.getRetryOnSame("test"));
  }

  @Test
  public void testIsRetryEnabledWithGetpropertyReturnsStringChar() {

    new MockUp<Configuration>() {
      @Mock
      private String getProperty(String defaultValue, String... keys) {
        return "tyt";
      }
    };

    Configuration.INSTANCE.isRetryEnabled("test");
    assertNotNull(Configuration.INSTANCE.isRetryEnabled("test"));
  }

  @Test
  public void testIsRetryEnabledWithGetpropertyReturnsStringNum() {

    new MockUp<Configuration>() {

      @Mock
      private String getProperty(String defaultValue, String... keys) {
        return "1234";
      }
    };

    Configuration.INSTANCE.isRetryEnabled("test");
    assertNotNull(Configuration.INSTANCE.isRetryEnabled("test"));
  }

  @Test
  public void testGetSuccessiveFailedTimes() {
    assertNotNull(Configuration.INSTANCE.getSuccessiveFailedTimes("test"));
  }

  @Test
  public void testGetSessionTimeoutInSeconds() {
    assertNotNull(Configuration.INSTANCE.getSessionTimeoutInSeconds("test"));
  }
}
