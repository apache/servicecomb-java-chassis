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

package org.apache.servicecomb.huaweicloud.servicestage;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.netflix.config.ConcurrentCompositeConfiguration;

public class TestAKSKAuthHeaderProvider {
  @Test
  public void test_project_name_properly_encoded_en() {
    ConcurrentCompositeConfiguration configuration = Mockito.mock(ConcurrentCompositeConfiguration.class);
    Mockito.when(configuration.getBoolean("servicecomb.credentials.akskEnabled", true)).thenReturn(true);
    Mockito.when(configuration.getString("servicecomb.credentials.project", "default")).thenReturn("hello");
    Mockito.when(configuration.getString("servicecomb.credentials.accessKey", "")).thenReturn("access key");
    Mockito.when(configuration.getString("servicecomb.credentials.secretKey", "")).thenReturn("secret key");
    Mockito.when(configuration.getString("servicecomb.credentials.akskCustomCipher", "default")).thenReturn("default");

    AKSKAuthHeaderProvider provider = new AKSKAuthHeaderProvider(configuration);
    Assert.assertEquals("hello", provider.authHeaders().get("X-Service-Project"), "hello");
  }

  @Test
  public void test_project_name_properly_encoded_cn() {
    ConcurrentCompositeConfiguration configuration = Mockito.mock(ConcurrentCompositeConfiguration.class);
    Mockito.when(configuration.getBoolean("servicecomb.credentials.akskEnabled", true)).thenReturn(true);
    Mockito.when(configuration.getString("servicecomb.credentials.project", "default")).thenReturn("测试");
    Mockito.when(configuration.getString("servicecomb.credentials.accessKey", "")).thenReturn("access key");
    Mockito.when(configuration.getString("servicecomb.credentials.secretKey", "")).thenReturn("secret key");
    Mockito.when(configuration.getString("servicecomb.credentials.akskCustomCipher", "default")).thenReturn("default");

    AKSKAuthHeaderProvider provider = new AKSKAuthHeaderProvider(configuration);
    Assert.assertEquals("hello", provider.authHeaders().get("X-Service-Project"), "%E6%B5%8B%E8%AF%95");
  }
}
