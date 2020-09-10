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

package org.apache.servicecomb.http.client.common;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MessageObjectMapper extends ObjectMapper {
  private static final long serialVersionUID = 189026839992490564L;

  public MessageObjectMapper() {
    getFactory().disable(Feature.AUTO_CLOSE_SOURCE);
    // Enable features that can tolerance errors and not enable those make more constraints for compatible reasons.
    // Developers can use validation api to do more checks.
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    // no view annotations shouldn't be included in JSON
    disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
    enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
  }
}
