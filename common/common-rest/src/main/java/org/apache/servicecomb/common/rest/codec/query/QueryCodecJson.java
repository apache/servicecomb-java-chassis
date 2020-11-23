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

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.apache.servicecomb.core.exception.ExceptionCodes;
import org.apache.servicecomb.core.exception.Exceptions;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

@Component
public class QueryCodecJson implements QueryCodec {
  public static final String CODEC_NAME = "json";

  private static final JavaType OBJECT_TYPE = TypeFactory.defaultInstance().constructType(Object.class);

  @Override
  public String getCodecName() {
    return CODEC_NAME;
  }

  @Override
  public void encode(URLPathStringBuilder builder, String name, @Nullable Object value) throws Exception {
    if (value == null) {
      return;
    }

    String json = RestObjectMapperFactory.getRestObjectMapper().writeValueAsString(value);
    builder.appendQuery(name, QueryCodec.encodeValue(json));
  }

  @Override
  public Object decode(QueryProcessor processor, HttpServletRequest request) {
    Object value = processor.getAndCheckParameter(request);
    if (value == null) {
      return null;
    }

    try {
      JavaType targetType = processor.getTargetType();
      if (targetType == null) {
        targetType = OBJECT_TYPE;
      }
      return RestObjectMapperFactory.getRestObjectMapper().readValue(value.toString(), targetType);
    } catch (JsonProcessingException e) {
      throw Exceptions
          .create(Status.BAD_REQUEST, ExceptionCodes.GENERIC_CLIENT,
              "failed to decode query parameter, name=" + processor.getParameterPath());
    }
  }
}
