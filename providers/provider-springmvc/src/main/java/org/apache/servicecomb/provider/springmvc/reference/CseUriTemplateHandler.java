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

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.servicecomb.common.rest.RestConst;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.util.DefaultUriTemplateHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 默认不支持下面第1个场景，需要做出修正
 * cse://app:ms/path to cse://app/ms/path
 * cse://ms/path to cse://ms/path
 */
public class CseUriTemplateHandler extends DefaultUriTemplateHandler {
  private Field hostField = ReflectionUtils.findField(UriComponentsBuilder.class, "host");

  public CseUriTemplateHandler() {
    ReflectionUtils.makeAccessible(hostField);
  }

  @Override
  protected URI expandInternal(String uriTemplate, Map<String, ?> uriVariables) {
    UriComponentsBuilder uriComponentsBuilder = initUriComponentsBuilder(uriTemplate);
    UriComponents uriComponents = expandAndEncode(uriComponentsBuilder, uriVariables);
    return createUri(uriTemplate, uriComponentsBuilder, uriComponents);
  }

  @Override
  protected URI expandInternal(String uriTemplate, Object... uriVariables) {
    UriComponentsBuilder uriComponentsBuilder = initUriComponentsBuilder(uriTemplate);
    UriComponents uriComponents = expandAndEncode(uriComponentsBuilder, uriVariables);
    return createUri(uriTemplate, uriComponentsBuilder, uriComponents);
  }

  private URI createUri(String uriTemplate, UriComponentsBuilder builder, UriComponents uriComponents) {
    String strUri = uriComponents.toUriString();

    if (isCrossApp(uriTemplate, builder)) {
      int idx = strUri.indexOf('/', RestConst.URI_PREFIX.length());
      strUri = strUri.substring(0, idx) + ":" + strUri.substring(idx + 1);
    }

    try {
      // Avoid further encoding (in the case of strictEncoding=true)
      return new URI(strUri);
    } catch (URISyntaxException ex) {
      throw new IllegalStateException("Could not create URI object: " + ex.getMessage(), ex);
    }
  }

  protected boolean isCrossApp(String uriTemplate, UriComponentsBuilder builder) {
    String host = (String) ReflectionUtils.getField(hostField, builder);
    int pos = RestConst.URI_PREFIX.length() + host.length();
    return uriTemplate.charAt(pos) == ':';
  }
}
