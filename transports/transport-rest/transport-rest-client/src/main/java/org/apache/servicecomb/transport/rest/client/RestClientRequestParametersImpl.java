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
package org.apache.servicecomb.transport.rest.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.PartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import jakarta.servlet.http.Part;

public class RestClientRequestParametersImpl implements RestClientRequestParameters {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestClientRequestParametersImpl.class);

  protected final MultiMap headers;

  protected Map<String, String> cookieMap;

  protected Map<String, Object> formMap;

  protected Multimap<String, Part> uploads;

  protected Buffer bodyBuffer;

  public RestClientRequestParametersImpl(MultiMap headers) {
    this.headers = headers;
  }

  @Override
  public Map<String, String> getCookieMap() {
    return cookieMap;
  }

  @Override
  public void addCookie(String name, String value) {
    if (cookieMap == null) {
      cookieMap = new HashMap<>();
    }

    cookieMap.put(name, value);
  }

  @Override
  public Map<String, Object> getFormMap() {
    return formMap;
  }

  @Override
  public void addForm(String name, Object value) {
    if (formMap == null) {
      formMap = new HashMap<>();
    }

    if (value != null) {
      formMap.put(name, value);
    }
  }

  @Override
  public MultiMap getHeaders() {
    return headers;
  }

  @Override
  public void putHeader(String name, String value) {
    headers.add(name, value);
  }

  @Override
  public Buffer getBodyBuffer() {
    return bodyBuffer;
  }

  @Override
  public void setBodyBuffer(Buffer bodyBuffer) {
    this.bodyBuffer = bodyBuffer;
  }

  @Override
  public Multimap<String, Part> getUploads() {
    return uploads;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void attach(String name, Object partOrList) {
    if (partOrList == null) {
      LOGGER.debug("null file is ignored, file name = [{}]", name);
      return;
    }

    if (uploads == null) {
      uploads = ArrayListMultimap.create();
    }

    if (partOrList.getClass().isArray()) {
      for (Object part : (Object[]) partOrList) {
        uploads.put(name, PartUtils.getSinglePart(name, part));
      }
      return;
    }

    if (partOrList instanceof Collection) {
      for (Object part : ((Collection<Object>) partOrList)) {
        uploads.put(name, PartUtils.getSinglePart(name, part));
      }
      return;
    }

    uploads.put(name, PartUtils.getSinglePart(name, partOrList));
  }
}
