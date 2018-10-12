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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.common.rest.codec.param.QueryProcessorCreator;
import org.apache.servicecomb.common.rest.definition.RestParam;

/**
 * 初始化阶段创建URLPathBuilder，用于加速调用阶段的path创建
 */
public class URLPathBuilder {

  private List<UrlParamWriter> pathParamWriterList = new ArrayList<>();

  private List<UrlParamWriter> queryParamWriterList = new ArrayList<>();

  private static final String SLASH = "/";

  public URLPathBuilder(String rawPath, Map<String, RestParam> paramMap) {
    initPathWriterList(rawPath, paramMap);
    initQueryWriterList(paramMap);
  }

  private void initQueryWriterList(Map<String, RestParam> paramMap) {
    for (RestParam param : paramMap.values()) {

      if (!QueryProcessorCreator.PARAMTYPE.equals(param.getParamProcessor().getProcessorType())) {
        continue;
      }

      UrlParamWriter dynamicWriter = new QueryVarParamWriter(param);
      queryParamWriterList.add(dynamicWriter);
    }
  }

  private void initPathWriterList(String rawPath, Map<String, RestParam> paramMap) {
    // 去掉末尾'/'
    if (rawPath.endsWith(SLASH)) {
      rawPath = rawPath.substring(0, rawPath.length() - 1);
    }
    // 首部加上'/'
    if (!rawPath.startsWith(SLASH)) {
      rawPath = SLASH + rawPath;
    }

    StringBuilder tmpPath = new StringBuilder();
    for (int idx = 0; idx < rawPath.length(); idx++) {
      char currentChar = rawPath.charAt(idx);
      if (currentChar == '{') {
        if (tmpPath.length() != 0) {
          this.pathParamWriterList.add(new StaticUrlParamWriter(tmpPath.toString()));
          tmpPath.setLength(0);
        }
      } else if (currentChar == '}') {
        if (tmpPath.length() != 0) {
          RestParam param = paramMap.get(tmpPath.toString());
          this.pathParamWriterList.add(new PathVarParamWriter(param));
          tmpPath.setLength(0);
        }
      } else {
        tmpPath.append(currentChar);
      }
    }
    if (tmpPath.length() != 0) {
      this.pathParamWriterList.add(new StaticUrlParamWriter(tmpPath.toString()));
    }
  }

  public String createRequestPath(Object[] args) throws Exception {
    URLPathStringBuilder builder = new URLPathStringBuilder();

    genPathString(builder, args);
    genQueryString(builder, args);

    return builder.build();
  }

  public String createPathString(Object[] args) throws Exception {
    URLPathStringBuilder builder = new URLPathStringBuilder();
    genPathString(builder, args);
    return builder.build();
  }

  private void genPathString(URLPathStringBuilder builder, Object[] args) throws Exception {
    for (UrlParamWriter writer : this.pathParamWriterList) {
      writer.write(builder, args);
    }
  }

  private void genQueryString(URLPathStringBuilder builder, Object[] args) throws Exception {
    for (UrlParamWriter writer : queryParamWriterList) {
      writer.write(builder, args);
    }
  }

  public static class URLPathStringBuilder {
    private StringBuilder stringBuilder = new StringBuilder();

    private boolean queryPrefixNotWrite = true;

    public URLPathStringBuilder appendPath(String s) {
      stringBuilder.append(s);
      return this;
    }

    public URLPathStringBuilder appendQuery(String key, String value) {
      if (queryPrefixNotWrite) {
        stringBuilder.append('?');
        queryPrefixNotWrite = false;
      } else {
        stringBuilder.append('&');
      }

      stringBuilder.append(key).append("=").append(value);
      return this;
    }

    public String build() {
      return stringBuilder.toString();
    }
  }
}
