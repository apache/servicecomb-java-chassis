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
package org.apache.servicecomb.swagger.generator.jaxrs.model.enums;

import org.apache.servicecomb.foundation.common.base.DynamicEnum;
import org.apache.servicecomb.foundation.common.base.DynamicEnumCache;

import com.fasterxml.jackson.annotation.JsonCreator;

import io.swagger.annotations.ApiModelProperty;

public class DynamicStatus extends DynamicEnum<Integer> {
  @ApiModelProperty(value = "dynamic bad request")
  public static final DynamicStatus BAD_REQUEST = new DynamicStatus(400);

  @ApiModelProperty(value = "dynamic not found")
  public static final DynamicStatus NOT_FOUND = new DynamicStatus(404);

  private static final DynamicEnumCache<DynamicStatus> CACHE = new DynamicEnumCache<>(DynamicStatus.class);

  public DynamicStatus(Integer value) {
    super(value);
  }

  @JsonCreator
  public static DynamicStatus fromValue(int value) {
    return CACHE.fromValue(value);
  }
}
