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

package org.apache.servicecomb.common.rest.codec.header;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.HeaderProcessorCreator.HeaderProcessor;

import jakarta.servlet.http.HttpServletRequest;

public interface HeaderCodec {
  static String encodeValue(Object value) throws UnsupportedEncodingException {
    return URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.name());
  }

  // can not be replaced by value.toString() because of date serialize
  static String convertToString(Object value) throws Exception {
    return RestObjectMapperFactory.getRestObjectMapper().convertToString(value);
  }

  String getCodecName();

  void encode(RestClientRequest clientRequest, String name, Object value) throws Exception;

  Object decode(HeaderProcessor processor, HttpServletRequest request);
}
