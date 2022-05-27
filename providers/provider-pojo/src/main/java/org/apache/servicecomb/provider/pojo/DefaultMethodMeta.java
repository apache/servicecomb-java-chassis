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
package org.apache.servicecomb.provider.pojo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.servicecomb.foundation.common.concurrent.ConcurrentHashMapEx;
import org.apache.servicecomb.foundation.common.utils.AsyncUtils;
import org.springframework.util.ReflectionUtils;

public class DefaultMethodMeta {
  private final Map<Method, MethodHandle> defaultMethodHandles = new ConcurrentHashMapEx<>();

  // java11
  private final Method privateLookupIn = ReflectionUtils.findMethod(MethodHandles.class,
      "privateLookupIn", Class.class, Lookup.class);

  // only for java8
  private Constructor<Lookup> lookupConstructor;

  public MethodHandle getOrCreateMethodHandle(Object proxy, Method method) {
    return defaultMethodHandles.computeIfAbsent(method, key -> createMethodHandle(proxy, method));
  }

  protected MethodHandle createMethodHandle(Object proxy, Method method) {
    try {
      if (privateLookupIn != null) {
        return createForJava11(proxy, method);
      }

      return createForJava8(proxy, method);
    } catch (Exception e) {
      AsyncUtils.rethrow(e);
      return null;
    }
  }

  protected MethodHandle createForJava11(Object proxy, Method method) throws Exception {
    Lookup lookup = MethodHandles.lookup();
    Lookup privateLookup = (Lookup) privateLookupIn.invoke(null, method.getDeclaringClass(), lookup);
    return privateLookup
        .unreflectSpecial(method, method.getDeclaringClass())
        .bindTo(proxy);
  }

  protected MethodHandle createForJava8(Object proxy, Method method) throws Exception {
    if (lookupConstructor == null) {
      lookupConstructor = ReflectionUtils.accessibleConstructor(Lookup.class, Class.class);
    }
    return lookupConstructor.newInstance(method.getDeclaringClass())
        .unreflectSpecial(method, method.getDeclaringClass())
        .bindTo(proxy);
  }
}
