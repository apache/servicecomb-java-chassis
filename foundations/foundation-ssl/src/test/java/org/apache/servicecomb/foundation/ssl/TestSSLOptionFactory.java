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

package org.apache.servicecomb.foundation.ssl;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.netflix.config.ConcurrentCompositeConfiguration;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestSSLOptionFactory {

  @Test
  public void testSSLOptionFactory() {
    SSLOptionFactory factory = SSLOptionFactory.createSSLOptionFactory("cc", null);
    Assertions.assertNull(factory);
  }

  @Test
  public void testSSLOptionFactoryWrong(@Mocked SSLOption option) {
    new Expectations() {
      {
        SSLOption.getStringProperty((ConcurrentCompositeConfiguration) any, anyString, (String[]) any);
        result = "wrong";
      }
    };

    IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
            () -> SSLOptionFactory.createSSLOptionFactory("cc", null));
    Assertions.assertEquals("Failed to create SSLOptionFactory.", exception.getMessage());
  }

  @Test
  public void testSSLOptionFactoryCorrent() {
    new MockUp<SSLOption>() {
      @Mock
      public String getStringProperty(ConcurrentCompositeConfiguration configSource, String defaultValue,
          String... keys) {
        return "org.apache.servicecomb.foundation.ssl.MyOptionFactory";
      }
    };
    SSLOptionFactory factory = SSLOptionFactory.createSSLOptionFactory("cc", null);
    Assertions.assertEquals(factory.createSSLOption().getProtocols(), "TLSv1.2");
  }
}
