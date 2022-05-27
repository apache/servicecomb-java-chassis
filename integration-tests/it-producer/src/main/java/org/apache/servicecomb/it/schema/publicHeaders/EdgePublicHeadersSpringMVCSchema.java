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
package org.apache.servicecomb.it.schema.publicHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HeaderParam;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "edgePublicHeadersSpringMVCSchema")
@RequestMapping(path = "/v1/edgePublicHeadersSpringMVCSchema")
public class EdgePublicHeadersSpringMVCSchema {

  @GetMapping("/requestHeaders")
  public String getRequestHeaders(@HeaderParam(value = "x_cse_test") String testHeader,
      HttpServletRequest request) {
    String external1 = request.getHeader("external_1");
    String external2 = request.getHeader("external_2");
    String external3 = request.getHeader("external_3");
    if (StringUtils.isEmpty(external3)) {
      return testHeader + "_" + external1 + "_" + external2;
    }
    return testHeader + "_" + external1 + "_" + external2 + "_" + external3;
  }
}
