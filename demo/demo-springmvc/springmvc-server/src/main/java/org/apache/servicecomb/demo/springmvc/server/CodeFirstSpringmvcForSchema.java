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

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created for testing schema to accommodate swagger api specification. When change this class, please use
 * http://editor.swagger.io/ to validate the generated schema and should not give any errors. In test case,
 * only checksum is validated to make sure schema is not changed.
 */
@RestSchema(schemaId = "CodeFirstSpringmvcForSchema")
@RequestMapping(path = "/forScheam")
public class CodeFirstSpringmvcForSchema {
  /*
   * Using http://editor.swagger.io/ . Listing errors not handled:
   *
   * #1. Should NOT have additional properties additionalProperty: type, format, name, in, required
   /reduce:
    get:
      operationId: "reduce"
      parameters:
      - name: "b"
        in: "cookie"
        required: false
   This schema gives error, but according to https://swagger.io/docs/specification/describing-parameters/#cookie-parameters
   This should supported.


   * #2. Operations with Parameters of "in: formData" must include "application/x-www-form-urlencoded" or "multipart/form-data" in their "consumes" property
   This error can be fixed by user code.


   *
   */


  @RequestMapping(path = "/uploadFile", method = RequestMethod.POST,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public boolean uploadAwardFile(@RequestParam("fileType") String fileType, @RequestParam("zoneId") String zoneId,
      @RequestPart("file") MultipartFile file) {
    throw new UnsupportedOperationException("only for testing schema");
  }
}
