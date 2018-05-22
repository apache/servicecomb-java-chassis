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

import java.lang.reflect.Field;

import org.apache.servicecomb.provider.springmvc.reference.CseClientHttpRequest;
import org.apache.servicecomb.provider.springmvc.reference.CseClientHttpResponse;
import org.apache.servicecomb.swagger.invocation.Response;
import org.springframework.util.ReflectionUtils;

import mockit.Mock;
import mockit.MockUp;

public class MockUtil {
  private static MockUtil instance = new MockUtil();

  private MockUtil() {

  }

  public static MockUtil getInstance() {
    return instance;
  }

  public void mockReflectionUtils() {

    new MockUp<ReflectionUtils>() {
      @Mock
      Object getField(Field field, Object target) {
        Response response = Response.ok(200);
        return new CseClientHttpResponse(response);
      }
    };
  }

  public void mockCseClientHttpRequest() {

    new MockUp<CseClientHttpRequest>() {
      @Mock
      public void setRequestBody(Object requestBody) {

      }
    };
  }
}
