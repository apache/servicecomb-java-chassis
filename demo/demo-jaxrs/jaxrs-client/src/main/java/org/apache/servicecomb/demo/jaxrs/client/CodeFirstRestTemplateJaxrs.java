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

package org.apache.servicecomb.demo.jaxrs.client;

import org.apache.servicecomb.demo.CodeFirstRestTemplate;
import org.apache.servicecomb.demo.TestMgr;
import org.springframework.web.client.RestTemplate;

public class CodeFirstRestTemplateJaxrs extends CodeFirstRestTemplate {
  @Override
  protected void testAllTransport(String microserviceName, RestTemplate template, String cseUrlPrefix) {
    testDefaultPath(template, cseUrlPrefix);

    super.testAllTransport(microserviceName, template, cseUrlPrefix);
  }

  private void testDefaultPath(RestTemplate template, String cseUrlPrefix) {
    int result =
        template.getForObject(cseUrlPrefix.substring(0, cseUrlPrefix.length() - 1), Integer.class);
    TestMgr.check(100, result);
  }
}
