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

package org.apache.servicecomb.swagger.converter.property;

import java.util.Collection;

public enum SwaggerParamCollectionFormat {
  CSV("csv", ","),
  SSV("ssv", " "),
  TSV("tsv", "\t"),
  PIPES("pipes", "|") {
    @Override
    public String[] splitParam(String rawParam) {
      if (null == rawParam) {
        return new String[0];
      }
      return rawParam.split("\\|", -1);
    }
  },
  MULTI("multi", null) {
    /**
     * In fact, {@link SwaggerParamCollectionFormat#MULTI#splitParam(String)} of {@link SwaggerParamCollectionFormat#MULTI}
     * should never be invoked. We just override this method to ensure it does not throw exception.
     */
    @Override
    public String[] splitParam(String rawParam) {
      if (null == rawParam) {
        return new String[0];
      }
      return new String[] {rawParam};
    }
  };

  final private String collectionFormat;

  final private String separator;

  SwaggerParamCollectionFormat(String collectionFormat, String separator) {
    this.collectionFormat = collectionFormat;
    this.separator = separator;
  }

  public String getCollectionFormat() {
    return collectionFormat;
  }

  public String getSeparator() {
    return separator;
  }

  public String[] splitParam(String rawParam) {
    if (null == rawParam) {
      return new String[0];
    }
    return rawParam.split(separator, -1);
  }

  /**
   * Join params with {@link #separator}.
   * Null element will be ignored since {@code null} cannot be described in query array param.
   *
   * @return joined params, or return {@code null} if {@code params} is null or all elements of {@code params} are null.
   */
  public String joinParam(Collection<?> params) {
    if (null == params || params.isEmpty()) {
      return null;
    }

    StringBuilder paramBuilder = new StringBuilder();
    int nullCount = 0;
    for (Object param : params) {
      if (param == null) {
        nullCount++;
        continue;
      }

      paramBuilder.append(param).append(separator);
    }
    if (nullCount == params.size()) {
      return null;
    }

    paramBuilder.setLength(paramBuilder.length() - 1);

    return paramBuilder.toString();
  }
}
