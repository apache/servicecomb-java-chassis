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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.Part;
import javax.ws.rs.core.HttpHeaders;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.foundation.common.part.FilePart;
import org.apache.servicecomb.foundation.common.part.InputStreamPart;
import org.apache.servicecomb.foundation.common.part.ResourcePart;
import org.apache.servicecomb.foundation.vertx.http.AbstractHttpServletRequest;
import org.springframework.core.io.Resource;

// restTemplate convert parameters to invocation args.
public class CommonToHttpServletRequest extends AbstractHttpServletRequest {
  private Map<String, List<String>> queryParams;

  private Map<String, List<String>> httpHeaders;

  // gen by httpHeaders
  private Cookie[] cookies;

  @SuppressWarnings("unchecked")
  public CommonToHttpServletRequest(Map<String, String> pathParams, Map<String, List<String>> queryParams,
      Map<String, List<String>> httpHeaders, Object bodyObject, boolean isFormData) {
    setAttribute(RestConst.PATH_PARAMETERS, pathParams);

    if (isFormData) {
      setAttribute(RestConst.FORM_PARAMETERS, (Map<String, Object>) bodyObject);
    } else {
      setAttribute(RestConst.BODY_PARAMETER, bodyObject);
    }

    this.queryParams = queryParams;
    this.httpHeaders = httpHeaders;
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
      return null;
    }

    return queryValues.toArray(new String[queryValues.size()]);
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

    return result.toArray(new Cookie[result.size()]);
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
    List<String> list = httpHeaders.computeIfAbsent(name, key -> {
      return new ArrayList<>();
    });
    list.add(value);
  }

  @Override
  public Part getPart(String name) throws IOException, ServletException {
    Object value = findPartInputValue(name);
    if (value == null) {
      return null;
    }

    if (Part.class.isInstance(value)) {
      return (Part) value;
    }

    if (InputStream.class.isInstance(value)) {
      return new InputStreamPart(name, (InputStream) value);
    }

    if (Resource.class.isInstance(value)) {
      return new ResourcePart(name, (Resource) value);
    }

    if (File.class.isInstance(value)) {
      return new FilePart(name, (File) value);
    }

    throw new IllegalStateException(
        String.format("File input parameter of %s could be %s / %s / %s or %s, but got %s.",
            name,
            Part.class.getName(),
            InputStream.class.getName(),
            Resource.class.getName(),
            File.class.getName(),
            value.getClass().getName()));
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
}
