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
package org.apache.servicecomb.swagger.invocation.converter.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.servicecomb.swagger.invocation.converter.Converter;

public final class SameElementArrayToSet implements Converter {
  private static final Converter INSTANCE = new SameElementArrayToSet();

  public static Converter getInstance() {
    return INSTANCE;
  }

  private SameElementArrayToSet() {
  }

  @Override
  public Object convert(Object value) {
    if (value == null) {
      return null;
    }

    Object[] array = (Object[]) value;
    Set<Object> set = new HashSet<>();
    set.addAll(Arrays.asList(array));
    return set;
  }
}
