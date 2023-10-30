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

  private final List<UrlParamWriter> pathParamWriterList = new ArrayList<>();

  private final List<UrlParamWriter> queryParamWriterList = new ArrayList<>();

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
        if (tmpPath.length() == 0) {
          continue;
        }
        String tmpPathStr = tmpPath.toString();
        String pathParamName = tmpPathStr;
        if (tmpPathStr.contains(":")) {
          pathParamName = tmpPathStr.split(":", 2)[0].trim();
        }
        RestParam param = paramMap.get(pathParamName);
        this.pathParamWriterList.add(new PathVarParamWriter(param));
        tmpPath.setLength(0);
      } else {
        tmpPath.append(currentChar);
      }
    }
    if (tmpPath.length() != 0) {
      this.pathParamWriterList.add(new StaticUrlParamWriter(tmpPath.toString()));
    }
  }

  public String createRequestPath(Map<String, Object> args) throws Exception {
    URLPathStringBuilder builder = new URLPathStringBuilder();

    genPathString(builder, args);
    genQueryString(builder, args);

    return builder.build();
  }

  public String createPathString(Map<String, Object> args) throws Exception {
    URLPathStringBuilder builder = new URLPathStringBuilder();
    genPathString(builder, args);
    return builder.build();
  }

  private void genPathString(URLPathStringBuilder builder, Map<String, Object> args) throws Exception {
    for (UrlParamWriter writer : this.pathParamWriterList) {
      writer.write(builder, args);
    }
  }

  private void genQueryString(URLPathStringBuilder builder, Map<String, Object> args) throws Exception {
    for (UrlParamWriter writer : queryParamWriterList) {
      writer.write(builder, args);
    }
  }

  public static class URLPathStringBuilder {
    private final StringBuilder stringBuilder = new StringBuilder();

    private boolean queryPrefixNotWrite = true;

    public URLPathStringBuilder appendPath(String s) {
      stringBuilder.append(s);
      return this;
    }

    public URLPathStringBuilder appendQuery(String name, String encodedValue) {
      if (queryPrefixNotWrite) {
        stringBuilder.append('?');
        queryPrefixNotWrite = false;
      } else {
        stringBuilder.append('&');
      }

      stringBuilder.append(name).append("=").append(encodedValue);
      return this;
    }

    public String build() {
      return stringBuilder.toString();
    }
  }
}
