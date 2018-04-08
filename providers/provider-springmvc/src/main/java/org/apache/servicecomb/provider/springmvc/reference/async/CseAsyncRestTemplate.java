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

package org.apache.servicecomb.provider.springmvc.reference.async;

import java.lang.reflect.Type;
import java.util.Arrays;

import org.apache.servicecomb.provider.springmvc.reference.CseUriTemplateHandler;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.AsyncRequestCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.CseHttpMessageConverter;

public class CseAsyncRestTemplate extends AsyncRestTemplate {
  public CseAsyncRestTemplate() {
    setMessageConverters(Arrays.asList(new CseHttpMessageConverter()));
    setAsyncRequestFactory(new CseAsyncClientHttpRequestFactory());
    setUriTemplateHandler(new CseUriTemplateHandler());
  }

  @Override
  protected <T> AsyncRequestCallback httpEntityCallback(HttpEntity<T> requestBody) {
    return new CseAsyncRequestCallback<T>(requestBody);
  }

  @Override
  protected <T> AsyncRequestCallback httpEntityCallback(HttpEntity<T> requestBody, Type responseType) {
    return new CseAsyncRequestCallback<T>(requestBody);
  }
}