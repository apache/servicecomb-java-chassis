/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.common.rest.codec;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import io.servicecomb.foundation.common.utils.HttpUtils;

public class LocalRestServerRequest implements RestServerRequest {
  private Map<String, String> pathParams;

  private Map<String, List<String>> queryParams;

  private Map<String, List<String>> httpHeaders;

  private Object bodyObject;

  public LocalRestServerRequest(Map<String, String> pathParams, Map<String, List<String>> queryParams,
      Map<String, List<String>> httpHeaders, Object bodyObject) {
    this.pathParams = pathParams;
    this.queryParams = queryParams;
    this.httpHeaders = httpHeaders;

    this.bodyObject = bodyObject;
  }

  @Override
  public String getPath() {
    throw new Error("no need to impl");
  }

  @Override
  public String getMethod() {
    throw new Error("not support");
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public String[] getQueryParam(String key) {
    List<String> values = queryParams.get(key);
    if (values == null) {
      return null;
    }

    return values.toArray(new String[values.size()]);
  }

  @Override
  public String getPathParam(String key) {
    return pathParams.get(key);
  }

  @Override
  public String getHeaderParam(String key) {
    List<String> headerValues = httpHeaders.get(key);
    return (headerValues != null ? headerValues.get(0) : null);
  }

  @Override
  public Object getFormParam(String key) {
    if (bodyObject == null) {
      return null;
    }

    // 走到form这里来，说明bodyObject必然是map
    @SuppressWarnings("unchecked")
    Map<String, Object> form = (Map<String, Object>) bodyObject;
    return form.get(key);
  }

  @Override
  public String getCookieParam(String key) {
    List<String> cookieList = httpHeaders.get(HttpHeaders.COOKIE);
    return HttpUtils.getCookieParamValue(key, cookieList);
  }

  @Override
  public Object getBody() throws Exception {
    return bodyObject;
  }

  @Override
  public Map<String, String[]> getQueryParams() {
    throw new Error("no need to impl");
  }
}
