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

package org.apache.servicecomb.common.rest.definition.path;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator.QueryProcessor;
import org.apache.servicecomb.common.rest.definition.RestParam;
import org.apache.servicecomb.common.rest.definition.path.URLPathBuilder.URLPathStringBuilder;
import org.apache.servicecomb.swagger.converter.property.SwaggerParamCollectionFormat;

public class QueryVarParamWriter extends AbstractUrlParamWriter {

  private SwaggerParamCollectionFormat collectionFormat;

  public QueryVarParamWriter(RestParam param) {
    this.param = param;
    this.collectionFormat = ((QueryProcessor) param.getParamProcessor()).getCollectionFormat();
  }

  @Override
  public void write(URLPathStringBuilder builder, Object[] args) throws Exception {
    Object value = getParamValue(args);
    if (value == null) {
      // do not write query key to express "null"
      return;
    }

    if (value.getClass().isArray()) {
      writeArray(builder, value);
      return;
    }

    if (Collection.class.isInstance(value)) {
      writeCollection(builder, value);
      return;
    }

    builder.appendQuery(param.getParamName(), encodeNotNullValue(value));
  }

  @SuppressWarnings("unchecked")
  private void writeCollection(URLPathStringBuilder builder, Object value) throws Exception {
    if (shouldJoinParams()) {
      writeJoinedParams(builder, (Collection<?>) value);
      return;
    }

    for (Object item : (Collection<Object>) value) {
      writeItem(builder, item);
    }
  }

  private void writeArray(URLPathStringBuilder builder, Object value) throws Exception {
    if (shouldJoinParams()) {
      writeJoinedParams(builder, Arrays.asList(((Object[]) value)));
      return;
    }

    for (Object item : (Object[]) value) {
      writeItem(builder, item);
    }
  }

  private void writeJoinedParams(URLPathStringBuilder builder, Collection<?> value) throws Exception {
    String joinedParam = collectionFormat.joinParam(value);
    if (null == joinedParam) {
      return;
    }
    builder.appendQuery(param.getParamName(), encodeNotNullValue(joinedParam));
  }

  /**
   * Whether to join params with separator.
   * For collection format csv/ssv/tsv/pipes
   */
  private boolean shouldJoinParams() {
    return null != collectionFormat && SwaggerParamCollectionFormat.MULTI != collectionFormat;
  }

  private void writeItem(URLPathStringBuilder builder, Object item) throws Exception {
    if (null == item) {
      return;
    }

    builder.appendQuery(param.getParamName(), encodeNotNullValue(item));
  }

  private String encodeNotNullValue(Object value) throws Exception {
    String strValue = RestObjectMapperFactory.getRestObjectMapper().convertToString(value);
    return URLEncoder.encode(strValue, StandardCharsets.UTF_8.name());
  }
}
