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

package org.apache.servicecomb.foundation.common.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class GenericsUtils {
  /**
   * check if XXX.class is generic type. see TestGenericsUtils for details meaning.
   * This method is provided for API compatibility for RestTemplate. Following code:
   *
   * <code>
   * List<GenericObjectParam<List<RecursiveObjectParam>>> response = consumers.getSCBRestTemplate()
   * postForObject("/testListObjectParam", request, List.class);
   * </code>
   *
   * should work for versions of 1.*. This is because java-chassis can read type info from swaggers.
   *
   * Start from 2.x, the best practise to write this code is to use ParameterizedTypeReference provided by RestTemplate
   * exchange method.
   */
  public static boolean isGenericResponseType(Type type) {
    if (type instanceof ParameterizedType) {
      return false;
    }
    if (type instanceof Class<?>) {
      return ((Class<?>) type).getTypeParameters().length > 0;
    }
    return true;
  }
}
