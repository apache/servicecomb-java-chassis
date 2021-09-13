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


import javax.ws.rs.FormParam;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.transport.rest.vertx.TransportConfig;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.vertx.core.http.HttpServerOptions;


@RestSchema(schemaId = "FormRequestSchema")
@RequestMapping(path = "/form", produces = MediaType.APPLICATION_JSON_VALUE)
public class FormRequestSchema {

  @PostMapping(path = "/formRequest")
  public String formRequest(@FormParam("formData") String formData, @FormParam("flag") String flag) throws Exception {
    if (formData.getBytes().length <= (flag == null ? HttpServerOptions.DEFAULT_MAX_FORM_ATTRIBUTE_SIZE
        : TransportConfig.getMaxFormAttributeSize())) {
      return "formRequest success";
    }
    return null;
  }

}
