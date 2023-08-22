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

import java.time.LocalDateTime;

import org.apache.servicecomb.foundation.test.scaffolding.model.User;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@SuppressWarnings("unused")
@RequestMapping(path = "MethodMixupAnnotations")
public class MethodMixupAnnotations {
  @RequestMapping(
      path = "usingRequestMapping/{targetName}",
      method = {RequestMethod.POST},
      consumes = {"text/plain", "application/json"},
      produces = {"text/plain", "application/json"})
  public String usingRequestMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word) {
    return String.format("%s %s %s %s", srcUser.name, header, targetName, word);
  }

  @GetMapping(
      path = "usingGetMapping/{targetName}",
      consumes = {"text/plain", "application/json"},
      produces = {"text/plain", "application/json"})
  public String usingGetMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word) {
    return String.format("%s %s %s %s", srcUser.name, header, targetName, word);
  }

  @PutMapping(
      path = "usingPutMapping/{targetName}",
      consumes = {"text/plain", "application/json"},
      produces = {"text/plain", "application/json"})
  public String usingPutMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word) {
    return String.format("%s %s %s %s", srcUser.name, header, targetName, word);
  }

  @PostMapping(
      path = "usingPostMapping/{targetName}",
      consumes = {"text/plain", "application/json"},
      produces = {"text/plain", "application/json"})
  public String usingPostMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word) {
    return String.format("%s %s %s %s", srcUser.name, header, targetName, word);
  }

  @PatchMapping(
      path = "usingPatchMapping/{targetName}",
      consumes = {"text/plain", "application/json"},
      produces = {"text/plain", "application/json"})
  public String usingPatchMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word) {
    return String.format("%s %s %s %s", srcUser.name, header, targetName, word);
  }

  @DeleteMapping(
      path = "usingDeleteMapping/{targetName}",
      consumes = {"text/plain", "application/json"},
      produces = {"text/plain", "application/json"})
  public String usingDeleteMapping(@RequestBody User srcUser, @RequestHeader String header,
      @PathVariable String targetName, @RequestParam(name = "word") String word) {
    return String.format("%s %s %s %s", srcUser.name, header, targetName, word);
  }

  @PostMapping(path = "/uploadFileAndAttribute")
  public String uploadFileAndAttribute(@RequestPart(name = "file") MultipartFile file,
      @RequestPart(name = "attribute") String attribute) {
    return null;
  }

  @PostMapping(path = "/uploadFilesAndAttribute")
  public String uploadFilesAndAttribute(@RequestPart(name = "files") MultipartFile[] files,
      @RequestPart(name = "attribute") String attribute) {
    return null;
  }

  @GetMapping(path = "/reduce")
  @Parameters({@Parameter(name = "a", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY)})
  public int reduce(HttpServletRequest request, @CookieValue(name = "b") int b) {
    return 0;
  }

  @RequestMapping(path = "/defaultQueryParam", method = RequestMethod.POST)
  public String defaultQueryParam(String prefix, @RequestBody User user) {
    return null;
  }

  @GetMapping(path = "/diffNames")
  @Operation(summary = "differentName", operationId = "differentName")
  public int diffNames(@RequestParam("x") int a, @RequestParam("y") int b) {
    return a * 2 + b;
  }

  @GetMapping(path = "/bytes")
  public byte[] bytes(@RequestBody byte[] value) {
    return null;
  }

  @PostMapping(path = "/upload", produces = MediaType.TEXT_PLAIN_VALUE)
  public String fileUpload(@RequestPart(name = "file1") MultipartFile file1,
      @RequestPart(name = "someFile") Part file2) {
    return null;
  }

  @PostMapping(path = "/testImplicitForm")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = {@Content(mediaType = SwaggerConst.FORM_MEDIA_TYPE,
          schema = @Schema(name = "form1", implementation = String.class,
              nullable = false, description = "a required form param")),
          @Content(mediaType = SwaggerConst.FORM_MEDIA_TYPE,
              schema = @Schema(name = "form2", implementation = String.class,
                  nullable = true, description = "an optional form param"))})
  public String testImplicitForm(HttpServletRequest request) {
    return null;
  }

  @GetMapping("/testDefaultValue")
  public String testDefaultValue(@RequestParam(name = "e", required = false) int e,
      @RequestHeader(name = "a", defaultValue = "20") int a,
      @CookieValue(name = "b", defaultValue = "bobo") String b,
      @RequestParam(name = "c", defaultValue = "40") Integer c,
      @Min(value = 20) @Max(value = 30) @RequestParam(name = "d", required = false) int d) {
    return "Hello " + a + b + c + d + e;
  }

  @GetMapping(path = "/testLocalDateTime")
  public LocalDateTime testLocalDateTime(@RequestParam("date") LocalDateTime date) {
    return date;
  }
}
