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
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;

public class CseHttpMessageConverter implements GenericHttpMessageConverter<Object> {

  private static final List<MediaType> ALL_MEDIA_TYPE = Arrays.asList(MediaType.ALL);

  @Override
  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return true;
  }

  @Override
  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return true;
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return ALL_MEDIA_TYPE;
  }

  @Override
  public Object read(Class<?> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
    throw new IllegalStateException("not supported");
  }

  private Object read(HttpInputMessage inputMessage) {
    throw new IllegalStateException("not supported");
  }

  @Override
  public void write(Object o, MediaType contentType,
      HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    write(o, outputMessage);
  }

  private void write(Object o, HttpOutputMessage outputMessage) {
    CseClientHttpRequest request = (CseClientHttpRequest) outputMessage;
    request.setRequestBody(o);
  }

  @Override
  public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
    return true;
  }

  @Override
  public Object read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    throw new IllegalStateException("not supported");
  }

  @Override
  public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
    return true;
  }

  @Override
  public void write(Object o, @Nullable Type type, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    write(o, outputMessage);
  }
}
