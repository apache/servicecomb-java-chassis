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
package org.apache.servicecomb.it.schema;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.extend.annotations.ResponseHeaders;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ResponseHeader;

@RestSchema(schemaId = "responseHeaderSchema")
@RequestMapping(path = "/v1/responseHeaderSchema")
public class ResponseHeaderSchema {
  @ResponseHeaders({@ResponseHeader(name = "h1", response = String.class)})
  @GetMapping(path = "/")
  public ResponseEntity<Void> responseHeader() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("h1", "h1Value");
    return new ResponseEntity<>(null, headers, HttpStatus.OK);
  }
}
