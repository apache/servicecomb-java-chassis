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
import java.util.StringJoiner;

import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;

import jakarta.servlet.http.HttpServletRequest;

/**
 * can not support value with delimiter<br>
 * a csv example, a collection with two values:<br>
 *   1. a<br>
 *   2. b,c<br>
 *  will encode to be a%2Cb%2Cc, this is ambiguous
 */
public class QueryCodecWithDelimiter extends AbstractQueryCodec {
  private final String joinDelimiter;

  private final String splitDelimiter;

  public QueryCodecWithDelimiter(String codecName, String joinDelimiter, String splitDelimiter) {
    super(codecName);
    this.joinDelimiter = joinDelimiter;
    this.splitDelimiter = splitDelimiter;
  }

  @Override
  public void encode(URLPathStringBuilder builder, String name, Collection<Object> values) throws Exception {
    String joined = join(values);
    if (joined == null) {
      return;
    }

    builder.appendQuery(name, joined);
  }

  /**
   * <pre>
   *   SwaggerIde:
   *   1. encode query value by uri rule, not url rule
   *      part of the difference:
   *               uri  url
   *        space  %20  +
   *        [      [    %5B
   *      some difference will cause tomcat parse url failed
   *      so we encode query value by url rule
   *   2. encode each element
   *        for pipes, SwaggerIde will encode [a, b] to a|b
   *        but this will cause problem when run with tomcat
   *        so we encode the joined value, not encode each element
   * </pre>
   * @param values values to be joined
   * @return joined value
   */
  protected String join(Collection<Object> values) throws Exception {
    StringJoiner joiner = new StringJoiner(joinDelimiter);
    boolean hasValue = false;
    for (Object value : values) {
      if (value != null) {
        String strValue = QueryCodec.convertToString(value);
        joiner.add(strValue);
        hasValue = true;
      }
    }

    return hasValue ? QueryCodec.encodeValue(joiner.toString()) : null;
  }

  @Override
  public Object decode(QueryProcessor processor, HttpServletRequest request) {
    Object value = processor.getAndCheckParameter(request);
    value = value != null ? value.toString().split(splitDelimiter, -1) : new String[0];
    return processor.convertValue(value);
  }
}
