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

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.netflix.config.ConcurrentCompositeConfiguration;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class TestSSLOptionFactory {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testSSLOptionFactory() {
    SSLOptionFactory factory = SSLOptionFactory.createSSLOptionFactory("cc", null);
    Assert.assertEquals(factory, null);
  }

  @Test
  public void testSSLOptionFactoryWrong(@Mocked SSLOption option) {
    new Expectations() {
      {
        SSLOption.getStringProperty((ConcurrentCompositeConfiguration) any, anyString, (String[]) any);
        result = "wrong";
      }
    };

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("Failed to create SSLOptionFactory."));
    SSLOptionFactory.createSSLOptionFactory("cc", null);
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
    Assert.assertEquals(factory.createSSLOption().getProtocols(), "TLSv1.2");
  }
}
