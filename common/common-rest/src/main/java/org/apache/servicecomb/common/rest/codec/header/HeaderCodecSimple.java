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

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.HeaderProcessorCreator.HeaderProcessor;

import jakarta.servlet.http.HttpServletRequest;

public class HeaderCodecSimple implements HeaderCodec {
  public static final String NAME = "simple";

  @Override
  public String getCodecName() {
    return NAME;
  }

  @Override
  public void encode(RestClientRequest clientRequest, String name, Object value) throws Exception {
    if (null == value) {
      // if value is empty, header should not be set to clientRequest to avoid NullPointerException in Netty.
      return;
    }
    clientRequest.putHeader(name,
        RestObjectMapperFactory.getConsumerWriterMapper().convertToString(value));
  }

  @Override
  public Object decode(HeaderProcessor processor, HttpServletRequest request) {
    Object value = request.getHeader(processor.getParameterPath());
    if (value == null) {
      value = processor.checkRequiredAndDefaultValue();
    }
    return processor.convertValue(value, processor.getTargetType());
  }
}
