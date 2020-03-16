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
package org.apache.servicecomb.it.schema.objectParams;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "springMVCObjectParamSchema")
@RequestMapping(path = "/v1/springMVCObjectParamSchema")
public class SpringMVCObjectParamSchema {

  /**
   * <a href="https://issues.apache.org/jira/browse/SCB-708">SCB-708</a> SpringMVC only
   */
  @GetMapping("testQueryObjectParam")
  public String testQueryObjectParam(QueryObjectModel QueryObjectModel) {
    return QueryObjectModel.toString();
  }

  @GetMapping("testQueryObjectWithHeader")
  public String testQueryObjectWithHeader(@RequestHeader("prefix") String prefix, QueryObjectModel QueryObjectModel) {
    return prefix + QueryObjectModel.toString();
  }

  @GetMapping("testQueryObjectWithHeaderName")
  public String testQueryObjectWithHeaderName(@RequestHeader(name = "prefix") String prefix,
      QueryObjectModel QueryObjectModel) {
    return prefix + QueryObjectModel.toString();
  }

  /**
   * <a href="https://issues.apache.org/jira/browse/SCB-1793">SCB-1793</a> support @RequestHeader(value ="xxx")
   */
  @GetMapping("testQueryObjectWithHeaderValue")
  public String testQueryObjectWithHeaderValue(@RequestHeader(value = "prefix") String prefix,
      QueryObjectModel QueryObjectModel) {
    return prefix + QueryObjectModel.toString();
  }

  @GetMapping("testQueryObjectWithHeaderValueAndName")
  public String testQueryObjectWithHeaderValueAndName(@RequestHeader(name = "prefix") String prefix,
      @RequestHeader(value = "suffix") String suffix, QueryObjectModel QueryObjectModel) {
    return prefix + QueryObjectModel.toString() + suffix;
  }

  @GetMapping("testQueryObjectWithParam")
  public String testQueryObjectWithParam(@RequestParam("prefix") String prefix, QueryObjectModel QueryObjectModel) {
    return prefix + QueryObjectModel.toString();
  }

  @GetMapping("testQueryObjectWithParamName")
  public String testQueryObjectWithParamName(@RequestParam(name = "prefix") String prefix,
      QueryObjectModel QueryObjectModel) {
    return prefix + QueryObjectModel.toString();
  }

  @GetMapping("testQueryObjectWithParamValue")
  public String testQueryObjectWithParamValue(@RequestParam(value = "prefix") String prefix,
      QueryObjectModel QueryObjectModel) {
    return prefix + QueryObjectModel.toString();
  }
}
