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

package org.apache.servicecomb.common.rest.codec;

import javax.servlet.http.Part;

import io.vertx.core.buffer.Buffer;

/**
 * vertx的HttpClientRequest没有getHeader的能力
 * 在写cookie参数时，没办法多次添加cookie，所以只能进行接口包装
 */
public interface RestClientRequest {
  void write(Buffer bodyBuffer);

  void end();

  void addCookie(String name, String value);

  void putHeader(String name, String value);

  void addForm(String name, Object value);

  Buffer getBodyBuffer() throws Exception;

  void attach(String name, Part part);
}
