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

package org.apache.servicecomb.demo.springmvc.client;

import java.util.Date;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RequestMapping(path = "/codeFirstSpringmvc", produces = MediaType.APPLICATION_JSON_VALUE)
public interface ThirdPartyService {
  @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Date.class))
      , description = "",
      headers = {@Header(name = "h1", schema = @Schema(implementation = String.class)),
          @Header(name = "h2", schema = @Schema(implementation = String.class))})
  @RequestMapping(path = "/responseEntity", method = RequestMethod.POST)
  ResponseEntity<Date> responseEntity(@RequestAttribute("date") Date date);
}
