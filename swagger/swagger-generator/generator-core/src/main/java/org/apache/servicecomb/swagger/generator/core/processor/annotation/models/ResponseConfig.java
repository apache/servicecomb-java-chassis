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
package org.apache.servicecomb.swagger.generator.core.processor.annotation.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.swagger.generator.core.processor.annotation.AnnotationUtils;

import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.responses.ApiResponse;

/**
 * Response的数据源太多，单单是标注都有N个
 * 所以将数据提取出来，统一处理
 */
public class ResponseConfig extends ResponseConfigBase {
  private int code;

  private List<ResponseHeaderConfig> responseHeaders;

  // 根据本config生成的response
  private ApiResponse response;

  private Example examples;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public List<ResponseHeaderConfig> getResponseHeaders() {
    return responseHeaders;
  }

  public void setResponseHeaders(Map<String, Header> responseHeaders) {
    this.responseHeaders = new ArrayList<>();
    for (Entry<String, Header> header : responseHeaders.entrySet()) {
      ResponseHeaderConfig config = AnnotationUtils.convert(header.getValue());
      if (config != null) {
        this.responseHeaders.add(config);
      }
    }

    if (this.responseHeaders.isEmpty()) {
      this.responseHeaders = null;
    }
  }

  public ApiResponse getResponse() {
    return response;
  }

  public void setResponse(ApiResponse response) {
    this.response = response;
  }

  public Example getExamples() {
    return examples;
  }

  public void setExamples(Example examples) {
    this.examples = examples;
  }
}
