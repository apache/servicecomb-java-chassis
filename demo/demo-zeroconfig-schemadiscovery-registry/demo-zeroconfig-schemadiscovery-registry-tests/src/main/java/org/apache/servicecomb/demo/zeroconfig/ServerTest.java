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

package org.apache.servicecomb.demo.zeroconfig;

import java.util.List;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ServerTest implements CategorizedTestCase {

  RestTemplate template = RestTemplateBuilder.create();

  @Override
  public void testRestTransport() {
    testServerGetName();
    testGetAllMicroservice();
  }

  private void testGetAllMicroservice() {
    // invoke demo-zeroconfig-schemadiscovery-registry-client
    TestMgr.check("2", template
        .getForObject(
            "cse://demo-zeroconfig-schemadiscovery-registry-client/register/url/prefix/getName?name=2",
            String.class));
  }

  @SuppressWarnings("rawTypes")
  private void testServerGetName() {
    // invoke demo-zeroconfig-schemadiscovery-registry-client
    TestMgr.check("2", template
        .getForObject(
            "cse://demo-zeroconfig-schemadiscovery-registry-client"
                + "/register/url/prefix/getRegisteredMicroservice",
            List.class).size());
  }
}
