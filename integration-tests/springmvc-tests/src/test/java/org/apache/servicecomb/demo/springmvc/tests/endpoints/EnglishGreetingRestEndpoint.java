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

package org.apache.servicecomb.demo.springmvc.tests.endpoints;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Profile("!SimplifiedMapping")
@RestSchema(schemaId = "englishGreetingRestEndpoint")
@RequestMapping(path = "/")
public class EnglishGreetingRestEndpoint extends EnglishGreetingRestEndpointBase {
  @RequestMapping(path = "/sayHi", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  @Override
  public String sayHi(@RequestParam("name") String name) {
    return super.sayHi(name);
  }

  @RequestMapping(path = "", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
  @ResponseBody
  @Override
  public String home() {
    return super.home();
  }
}
