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

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;

import org.apache.servicecomb.common.rest.RestConst;
import org.springframework.web.client.CseHttpMessageConverter;
import org.springframework.web.client.RequestCallback;

public class CseRestTemplate extends AcceptableRestTemplate {
  public CseRestTemplate() {
    setMessageConverters(Arrays.asList(new CseHttpMessageConverter()));
    setRequestFactory(new CseClientHttpRequestFactory());
    setUriTemplateHandler(new CseUriTemplateHandler());
  }

  @Override
  protected <T> RequestCallback httpEntityCallback(Object requestBody) {
    RequestCallback callback = super.httpEntityCallback(requestBody);
    CseRequestCallback cseCallback = new CseRequestCallback(requestBody, callback);
    return cseCallback;
  }

  @Override
  protected <T> RequestCallback httpEntityCallback(Object requestBody, Type responseType) {
    RequestCallback callback = super.httpEntityCallback(requestBody, responseType);
    CseRequestCallback cseCallback = new CseRequestCallback(requestBody, callback);
    return cseCallback;
  }

  @Override
  public boolean isAcceptable(String uri) {
    return uri.startsWith(RestConst.URI_PREFIX);
  }

  @Override
  public boolean isAcceptable(URI uri) {
    return RestConst.SCHEME.equals(uri.getScheme());
  }
}
