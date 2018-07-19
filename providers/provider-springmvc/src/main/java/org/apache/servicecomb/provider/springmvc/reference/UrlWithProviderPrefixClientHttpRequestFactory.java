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

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

/**
 * When deploying in a container, like tomcat, users want to invoke service with full path, including container context
 * root and servlet path.
 */
public class UrlWithProviderPrefixClientHttpRequestFactory implements ClientHttpRequestFactory {
  static class UrlWithProviderPrefixClientHttpRequest extends CseClientHttpRequest {
    private String prefix;

    public UrlWithProviderPrefixClientHttpRequest(URI uri, HttpMethod httpMethod, String prefix) {
      super(uri, httpMethod);
      this.prefix = prefix;
    }

    @Override
    protected String findUriPath(URI uri) {
      return uri.getRawPath().substring(prefix.length());
    }
  }

  private String prefix;

  public UrlWithProviderPrefixClientHttpRequestFactory(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
    return new UrlWithProviderPrefixClientHttpRequest(uri, httpMethod, prefix);
  }
}
