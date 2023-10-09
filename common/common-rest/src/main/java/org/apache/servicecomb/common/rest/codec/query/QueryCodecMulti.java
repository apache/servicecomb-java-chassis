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
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ?query=x1&query=x2
 */
@SuppressWarnings("unchecked")
public class QueryCodecMulti extends AbstractQueryCodec {
  public static final String CODEC_NAME = "form:1";

  public QueryCodecMulti() {
    super(CODEC_NAME);
  }

  @Override
  public void encode(URLPathStringBuilder builder, String name, Collection<Object> values) throws Exception {
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

      // compatible to SpringMVC @RequestParam. BODY_PARAMETER is only set for SpringMVC.
      if (values == null || values.length == 0) {
        Map<String, Object> forms = (Map<String, Object>) request.getAttribute(RestConst.BODY_PARAMETER);
        if (forms == null) {
          return processor.convertValue(values);
        }
        Object formValue = forms.get(processor.getParameterPath());
        if (formValue == null) {
          return processor.convertValue(values);
        }
        if (formValue instanceof String[]) {
          values = (String[]) formValue;
        } else {
          values = new String[] {formValue.toString()};
        }
      }
      return processor.convertValue(values);
    }

    Object value = processor.getAndCheckParameter(request);
    return processor.convertValue(value);
  }
}
