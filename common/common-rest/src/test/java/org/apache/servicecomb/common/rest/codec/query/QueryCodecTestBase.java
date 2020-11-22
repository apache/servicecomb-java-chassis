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

import static org.assertj.core.api.Assertions.assertThat;

import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;

import com.fasterxml.jackson.databind.type.TypeFactory;

import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;

public class QueryCodecTestBase {
  QueryCodec codec;

  String queryName = "q";

  void should_encode(String encodedValue, Object... values) throws Exception {
    URLPathStringBuilder builder = new URLPathStringBuilder();
    codec.encode(builder, queryName, values);

    assertThat(builder.build()).isEqualTo(encodedValue);
  }

  void should_decode(String value, Object decodedValue) {
    HttpServletRequest Request = new AbstractHttpServletRequest() {
      @Override
      public String getParameter(String name) {
        return value;
      }
    };

    should_decode(Request, decodedValue);
  }

  void should_decode(String[] value, Object decodedValue) {
    HttpServletRequest Request = new AbstractHttpServletRequest() {
      @Override
      public String[] getParameterValues(String name) {
        return value;
      }
    };

    should_decode(Request, decodedValue);
  }

  private void should_decode(HttpServletRequest request, Object decodedValue) {
    Class<?> targetType = decodedValue == null ? Object.class : decodedValue.getClass();

    QueryParameter queryParameter = new QueryParameter();
    queryParameter.setCollectionFormat(codec.getCodecName());
    if (targetType.isArray()) {
      queryParameter.setType(ArrayProperty.TYPE);
    }

    QueryProcessor queryProcessor = new QueryProcessor(queryParameter,
        TypeFactory.defaultInstance().constructType(targetType));

    Object values = codec.decode(queryProcessor, request);

    assertThat(values).isEqualTo(decodedValue);
  }
}
