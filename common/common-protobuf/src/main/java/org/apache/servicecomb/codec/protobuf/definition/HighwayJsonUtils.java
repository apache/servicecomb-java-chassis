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
package org.apache.servicecomb.codec.protobuf.definition;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.apache.servicecomb.foundation.common.utils.RestObjectMapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Add a separate converter for highway. Now highway compile types depends on swagger information, do not use any consumer information,
 * This will cause any invocation will convert swagger raw types to consumer types. This will lose performance.
 *
 * In later improvements, need add consumer information to OperationProtobuf.
 *
 * Why not using RestObjectMapper? Because the reason above, highway use java.util.Date for date-time, and will serialize to a String
 * that can not convert to LocalDateTime. (This is a jackson change to format java.util.Date to `2017-07-21T16:32:28.320+0000`
 * but jackson can only convert `2017-07-21T16:32:28.320Z` to LocalDateTime
 */
public class HighwayJsonUtils {

  public static final ObjectMapper OBJ_MAPPER = new RestObjectMapper();


  private HighwayJsonUtils() {

  }

  @SuppressWarnings("unchecked")
  public static <T> T convertValue(Object fromValue, JavaType toValueType) {
    if (fromValue == null) {
      return null;
    }

    if (TypeFactory.defaultInstance().constructType(LocalDateTime.class).equals(toValueType)) {
      // jackson do not have a proper converter for this.
      if (fromValue instanceof Date) {
        return (T) LocalDateTime.ofInstant(Instant.ofEpochMilli(((Date) fromValue).getTime()), ZoneOffset.UTC);
      }
    }
    return OBJ_MAPPER.convertValue(fromValue, toValueType);
  }
}
