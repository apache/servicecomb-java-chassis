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
package org.apache.servicecomb.common.rest.codec.query;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.springframework.core.Ordered;

import jakarta.servlet.http.HttpServletRequest;

/**
 * bigger order will override the same name codec
 */
public interface QueryCodec extends Ordered {
  static String encodeValue(Object value) throws UnsupportedEncodingException {
    return URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.name());
  }

  // can not replaced by value.toString() because of date serialize
  static String convertToString(Object value) throws Exception {
    return RestObjectMapperFactory.getRestObjectMapper().convertToString(value);
  }

  @Override
  default int getOrder() {
    return 0;
  }

  String getCodecName();

  void encode(URLPathStringBuilder builder, String name, Object value) throws Exception;

  Object decode(QueryProcessor processor, HttpServletRequest request);
}
