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

package org.apache.servicecomb.provider.common;

import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import mockit.Mock;
import mockit.MockUp;

public class MockUtil {

  private static final MockUtil instance = new MockUtil();

  private MockUtil() {

  }

  public static MockUtil getInstance() {
    return instance;
  }

  public void mockBeanUtils() {

    new MockUp<BeanUtils>() {
      @Mock
      ApplicationContext getContext() {
        return Mockito.mock(ApplicationContext.class);
      }

      @Mock
      <T> T getBean(String name) {
        return null;
      }
    };
  }

  public void mockBeanUtilsObject() {

    new MockUp<BeanUtils>() {
      @Mock
      ApplicationContext getContext() {
        return Mockito.mock(ApplicationContext.class);
      }

      @SuppressWarnings("unchecked")
      @Mock
      <T> T getBean(String name) {
        return (T) new Object();
      }
    };
  }
}
