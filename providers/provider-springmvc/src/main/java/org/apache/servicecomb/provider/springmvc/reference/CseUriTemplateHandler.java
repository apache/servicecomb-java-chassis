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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.servicecomb.common.rest.RestConst;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 默认不支持下面第1个场景，需要做出修正
 * cse://app:ms/path to cse://app/ms/path
 * cse://ms/path to cse://ms/path
 */
@SuppressWarnings("deprecation")
// TODO : upgrade to spring 5 will having warning's , we'll fix it later
public class CseUriTemplateHandler extends org.springframework.web.util.DefaultUriTemplateHandler {
  private static final String SCHEME_PATTERN = "([^:/?#]+):";

  private static final String USERINFO_PATTERN = "([^@\\[/?#]*)";

  private static final String HOST_IPV4_PATTERN = "[^\\[/?#:]*";

  private static final String HOST_IPV6_PATTERN = "\\[[\\p{XDigit}\\:\\.]*[%\\p{Alnum}]*\\]";

  private static final String HOST_PATTERN = "(" + HOST_IPV6_PATTERN + "|" + HOST_IPV4_PATTERN + ")";

  private static final String PORT_PATTERN = "(\\d*(?:\\{[^/]+?\\})?)";

  private static final String PATH_PATTERN = "([^?#]*)";

  private static final String QUERY_PATTERN = "([^#]*)";

  private static final String LAST_PATTERN = "(.*)";

  // Regex patterns that matches URIs. See RFC 3986, appendix B
  private static final Pattern URI_PATTERN = Pattern.compile(
      "^(" + SCHEME_PATTERN + ")?" + "(//(" + USERINFO_PATTERN + "@)?" + HOST_PATTERN + "(:" + PORT_PATTERN +
          ")?" + ")?" + PATH_PATTERN + "(\\?" + QUERY_PATTERN + ")?" + "(#" + LAST_PATTERN + ")?");

  public CseUriTemplateHandler() {
    setStrictEncoding(true);
  }

  @Override
  protected URI expandInternal(String uriTemplate, Map<String, ?> uriVariables) {
    UriComponentsBuilder uriComponentsBuilder = initUriComponentsBuilder(uriTemplate);
    UriComponents uriComponents = expandAndEncode(uriComponentsBuilder, uriVariables);
    return createUri(uriTemplate, uriComponents);
  }

  @Override
  protected URI expandInternal(String uriTemplate, Object... uriVariables) {
    UriComponentsBuilder uriComponentsBuilder = initUriComponentsBuilder(uriTemplate);
    UriComponents uriComponents = expandAndEncode(uriComponentsBuilder, uriVariables);
    return createUri(uriTemplate, uriComponents);
  }

  private URI createUri(String uriTemplate, UriComponents uriComponents) {
    String strUri = uriComponents.toUriString();

    Matcher matcher = URI_PATTERN.matcher(uriTemplate);
    matcher.matches(); // should always be true
    String scheme = matcher.group(2);
    String host = matcher.group(6);

    if (isCrossApp(uriTemplate, scheme, host)) {
      int idx;
      if (RestConst.SCHEME.equals(scheme)) {
        idx = strUri.indexOf('/', RestConst.URI_PREFIX.length());
      } else {
        idx = strUri.indexOf('/', RestConst.URI_PREFIX_NEW.length());
      }
      strUri = strUri.substring(0, idx) + ":" + strUri.substring(idx + 1);
    }

    try {
      // Avoid further encoding (in the case of strictEncoding=true)
      return new URI(strUri);
    } catch (URISyntaxException ex) {
      throw new IllegalStateException("Could not create URI object: " + ex.getMessage(), ex);
    }
  }

  private boolean isCrossApp(String uriTemplate, String scheme, String host) {
    int pos;
    if (RestConst.SCHEME.equals(scheme)) {
      pos = RestConst.URI_PREFIX.length() + host.length();
    } else {
      pos = RestConst.URI_PREFIX_NEW.length() + host.length();
    }

    return uriTemplate.charAt(pos) == ':';
  }
}
