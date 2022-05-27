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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.springframework.stereotype.Component;

@Component
public class QueryCodecMulti extends AbstractQueryCodec {
  public static final String CODEC_NAME = "multi";

  public QueryCodecMulti() {
    super(CODEC_NAME);
  }

  @Override
  public void encode(URLPathStringBuilder builder, String name, @Nonnull Collection<Object> values) throws Exception {
    for (Object value : values) {
      if (value == null) {
        continue;
      }

      String strValue = QueryCodec.convertToString(value);
      builder.appendQuery(name, QueryCodec.encodeValue(strValue));
    }
  }

  @Override
  public Object decode(QueryProcessor processor, HttpServletRequest request) {
    if (processor.isRepeatedType()) {
      //Even if the paramPath does not exist, value won't be null at now
      String[] values = request.getParameterValues(processor.getParameterPath());
      return processor.convertValue(values);
    }

    Object value = processor.getAndCheckParameter(request);
    return processor.convertValue(value);
  }
}
