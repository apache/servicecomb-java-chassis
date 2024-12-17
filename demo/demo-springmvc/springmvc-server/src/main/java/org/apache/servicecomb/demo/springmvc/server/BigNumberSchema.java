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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "BigNumberSchema")
@RequestMapping("/bigNumber")
public class BigNumberSchema {
  @PostMapping(path = "/integer", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public BigInteger bigInteger(@RequestHeader("intHeader") BigInteger intHeader,
      @RequestParam("intQuery") BigInteger intQuery, @RequestAttribute("intForm") BigInteger intForm) {
    return intHeader.add(intQuery).add(intForm);
  }

  @PostMapping(path = "/decimal", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public BigDecimal bigDecimal(@RequestHeader("decimalHeader") BigDecimal decimalHeader,
      @RequestParam("decimalQuery") BigDecimal decimalQuery, @RequestAttribute("decimalForm") BigDecimal decimalForm) {
    return decimalHeader.add(decimalQuery).add(decimalForm);
  }
}
