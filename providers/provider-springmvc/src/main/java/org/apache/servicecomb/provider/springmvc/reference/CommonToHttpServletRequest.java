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

package org.apache.servicecomb.provider.springmvc.reference;

import static org.apache.servicecomb.foundation.common.utils.PartUtils.getSinglePart;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;

import com.google.common.annotations.VisibleForTesting;

// restTemplate convert parameters to invocation args.
public class CommonToHttpServletRequest extends AbstractHttpServletRequest {
  private final Map<String, List<String>> queryParams;

  private final Map<String, List<String>> httpHeaders;

  //contains all the file key in the parts
  private List<String> fileKeys = new ArrayList<>();

  // gen by httpHeaders
  private Cookie[] cookies;

  @SuppressWarnings("unchecked")
  public CommonToHttpServletRequest(Map<String, String> pathParams, Map<String, List<String>> queryParams,
      Map<String, List<String>> httpHeaders, Object bodyObject, boolean isFormData, List<String> fileKeys) {
    setAttribute(RestConst.PATH_PARAMETERS, pathParams);
    this.fileKeys = fileKeys;
    if (isFormData) {
      setAttribute(RestConst.FORM_PARAMETERS, (Map<String, Object>) bodyObject);
    } else {
      setAttribute(RestConst.BODY_PARAMETER, bodyObject);
    }

    this.queryParams = queryParams;
    this.httpHeaders = httpHeaders;
  }

  @SuppressWarnings("unchecked")
  public CommonToHttpServletRequest(Map<String, String> pathParams, Map<String, List<String>> queryParams,
      Map<String, List<String>> httpHeaders, Object bodyObject, boolean isFormData) {
    this(pathParams, queryParams, httpHeaders, bodyObject, isFormData, null);
  }

  @Override
  public String getContentType() {
    return getHeader(HttpHeaders.CONTENT_TYPE);
  }

  // not include form data, only for query
  @Override
  public String getParameter(String name) {
    List<String> queryValues = queryParams.get(name);
    if (queryValues == null || queryValues.isEmpty()) {
      return null;
    }

    return queryValues.get(0);
  }

  // not include form data, only for query
  @Override
  public String[] getParameterValues(String name) {
    List<String> queryValues = queryParams.get(name);
    if (queryValues == null || queryValues.isEmpty()) {
      return new String[0];
    }

    return queryValues.toArray(new String[0]);
  }

  @Override
  public String getHeader(String name) {
    List<String> headerValues = httpHeaders.get(name);
    if (headerValues == null || headerValues.isEmpty()) {
      return null;
    }

    return headerValues.get(0);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    List<String> headerValues = httpHeaders.get(name);
    if (headerValues == null || headerValues.isEmpty()) {
      return Collections.emptyEnumeration();
    }

    return Collections.enumeration(headerValues);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return Collections.enumeration(httpHeaders.keySet());
  }

  @Override
  public Cookie[] getCookies() {
    if (cookies == null) {
      cookies = createCookies();
    }

    return cookies;
  }

  private Cookie[] createCookies() {
    List<String> strCookies = httpHeaders.get(HttpHeaders.COOKIE);
    if (strCookies == null) {
      return new Cookie[] {};
    }

    List<Cookie> result = new ArrayList<>();
    for (String strCookie : strCookies) {
      List<HttpCookie> httpCookies = HttpCookie.parse(strCookie);
      for (HttpCookie httpCookie : httpCookies) {
        Cookie cookie = new Cookie(httpCookie.getName(), httpCookie.getValue());
        result.add(cookie);
      }
    }

    return result.toArray(new Cookie[0]);
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return null;
  }

  @Override
  public void setHeader(String name, String value) {
    httpHeaders.put(name, Arrays.asList(value));
  }

  @Override
  public void addHeader(String name, String value) {
    List<String> list = httpHeaders.computeIfAbsent(name, key -> new ArrayList<>());
    list.add(value);
  }

  @Override
  public Part getPart(String name) {
    Object value = findPartInputValue(name);
    return getSinglePart(name, value);
  }

  @Override
  public Collection<Part> getParts() {
    @SuppressWarnings("unchecked")
    Map<String, Object> form = (Map<String, Object>) getAttribute(RestConst.FORM_PARAMETERS);
    List<Part> partList = new ArrayList<>();
    filePartListWithForm(partList, form);
    return partList;
  }

  private void filePartListWithForm(List<Part> partList, Map<String, Object> form) {
    for (String key : fileKeys) {
      Object value = form.get(key);
      if (value == null) {
        continue;
      }
      if (Collection.class.isInstance(value)) {
        Collection<?> collection = (Collection<?>) value;
        for (Object part : collection) {
          partList.add(getSinglePart(key, part));
        }
        continue;
      }
      if (value.getClass().isArray()) {
        Object[] params = (Object[]) value;
        for (Object param : params) {
          partList.add(getSinglePart(key, param));
        }
        continue;
      }
      partList.add(getSinglePart(key, value));
    }
  }

  protected Object findPartInputValue(String name) {
    @SuppressWarnings("unchecked")
    Map<String, Object> form = (Map<String, Object>) getAttribute(RestConst.FORM_PARAMETERS);
    Object value = form.get(name);
    if (value == null) {
      return null;
    }

    if (Collection.class.isInstance(value)) {
      Collection<?> collection = (Collection<?>) value;
      if (collection.isEmpty()) {
        return null;
      }

      value = collection.iterator().next();
    }
    return value;
  }

  @VisibleForTesting
  public List<String> getFileKeys() {
    return fileKeys;
  }
}
