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
import java.lang.reflect.Type;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.client.ResponseExtractor;

public class CseResponseEntityResponseExtractor<T> implements ResponseExtractor<ResponseEntity<T>> {
  @Nullable
  private final CseHttpMessageConverterExtractor<T> delegate;

  public CseResponseEntityResponseExtractor(@Nullable Type responseType) {
    if (responseType != null && Void.class != responseType) {
      this.delegate = new CseHttpMessageConverterExtractor<>();
    } else {
      this.delegate = null;
    }
  }

  @Override
  public ResponseEntity<T> extractData(ClientHttpResponse response) throws IOException {
    if (this.delegate != null) {
      T body = this.delegate.extractData(response);
      return ResponseEntity.status(response.getRawStatusCode()).headers(response.getHeaders()).body(body);
    } else {
      return ResponseEntity.status(response.getRawStatusCode()).headers(response.getHeaders()).build();
    }
  }
}
