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

package org.apache.servicecomb.demo.springmvc.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestSchema(schemaId = "contentTypeSpringmvc")
@RequestMapping("/contentTypeSpringmvc")
@Api(consumes = MediaType.TEXT_PLAIN, produces = MediaType.TEXT_PLAIN)
public class ContentTypeSpringmvc {
  @RequestMapping(path = "/testGlobalSetting", method = RequestMethod.POST)
  public String testGlobalSetting(@RequestBody String name, HttpServletRequest request) {
    return String.format("testGlobalSetting: name=[%s], request content-type=[%s]", name, request.getContentType());
  }

  @RequestMapping(path = "/testApiOperation", method = RequestMethod.POST)
  @ApiOperation(value = "testApiOperation desc", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public String testApiOperation(@RequestBody String name, HttpServletRequest request) {
    return String.format("testApiOperation: name=[%s], request content-type=[%s]", name, request.getContentType());
  }

  @RequestMapping(path = "/testRequestMapping", method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
  public String testRequestMapping(@RequestBody String name, HttpServletRequest request) {
    return String.format("testRequestMapping: name=[%s], request content-type=[%s]", name, request.getContentType());
  }
}
