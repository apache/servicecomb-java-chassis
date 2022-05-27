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

package org.apache.servicecomb.it.schema;

import org.apache.servicecomb.foundation.common.base.DynamicEnum;
import org.apache.servicecomb.foundation.common.base.DynamicEnumCache;

import com.fasterxml.jackson.annotation.JsonCreator;

public class DynamicColor extends DynamicEnum<String> {
  public static final DynamicColor RED = new DynamicColor("RED");

  public static final DynamicColor YELLOW = new DynamicColor("YELLOW");

  public static final DynamicColor BLUE = new DynamicColor("BLUE");

  private static final DynamicEnumCache<DynamicColor> CACHE = new DynamicEnumCache<>(DynamicColor.class);

  public DynamicColor(String value) {
    super(value);
  }

  @JsonCreator
  public static DynamicColor fromValue(String value) {
    return CACHE.fromValue(value);
  }
}
