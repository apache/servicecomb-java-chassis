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

package org.apache.servicecomb.swagger.generator.springmvc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.foundation.test.scaffolding.model.Color;
import org.apache.servicecomb.swagger.extend.annotations.RawJsonRequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import jakarta.ws.rs.core.MediaType;

@RequestMapping(
    path = "Echo",
    method = {RequestMethod.PUT},
    consumes = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN},
    produces = {MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
public class Echo {

  @RequestMapping("emptyPath")
  public void emptyPath() {
  }

  @RequestMapping("inheritHttpMethod")
  public void inheritHttpMethod(int query) {
  }

  @RequestMapping("cookie")
  public void cookie(@CookieValue(value = "cookie", required = false) int cookie) {
  }

  @RequestMapping("rawJsonStringMethod")
  public void rawJsonStringMethod(@RawJsonRequestBody String jsonInput) {
  }

  @RequestMapping("enumBody")
  public void enumBody(@RequestBody Color color) {
  }

  @RequestMapping("asyncResponseEntity")
  public CompletableFuture<ResponseEntity<List<String>>> asyncResponseEntity() {
    return null;
  }

  @RequestMapping("testResponseEntityOptional")
  public ResponseEntity<Optional<String>> testResponseEntityOptional() {
    return null;
  }

  @RequestMapping("testCompletableFutureResponseEntityOptional")
  public CompletableFuture<ResponseEntity<Optional<String>>> testCompletableFutureResponseEntityOptional() {
    return null;
  }

  @RequestMapping(value = "part", consumes = MediaType.MULTIPART_FORM_DATA)
  public void part(@RequestPart MultipartFile part) {

  }

  @RequestMapping(value = "partArray", consumes = MediaType.MULTIPART_FORM_DATA)
  public void partArray(@RequestPart MultipartFile[] part) {

  }

  @RequestMapping(value = "partList", consumes = MediaType.MULTIPART_FORM_DATA)
  public void partList(@RequestPart List<MultipartFile> part) {

  }

  @RequestMapping(value = "partAnnotation", consumes = MediaType.MULTIPART_FORM_DATA)
  public void partAnnotation(@RequestPart MultipartFile part) {

  }

  @RequestMapping(value = "partArrayAnnotation", consumes = MediaType.MULTIPART_FORM_DATA)
  public void partArrayAnnotation(@RequestPart MultipartFile[] part) {

  }

  @RequestMapping(value = "partListAnnotation", consumes = MediaType.MULTIPART_FORM_DATA)
  public void partListAnnotation(@RequestPart List<MultipartFile> part) {

  }

  @RequestMapping("nestedListString")
  public List<List<String>> nestedListString(@RequestBody List<List<String>> param) {
    return param;
  }
}
