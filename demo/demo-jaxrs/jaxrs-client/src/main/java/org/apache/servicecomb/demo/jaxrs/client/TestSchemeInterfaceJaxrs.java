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

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class TestSchemeInterfaceJaxrs implements CategorizedTestCase {
  @RpcReference(schemaId = "SchemeInterfaceJaxrs", microserviceName = "jaxrs")
  private SchemeInterfaceJaxrs jaxrs;

  public void testAllTransport() throws Exception {
    TestMgr.check(3, jaxrs.add(1, 2));

    try {
      jaxrs.reduce(1, 3);
      TestMgr.failed("should throw exception", new Exception());
    } catch (Exception e) {
      TestMgr.check(
          "Consumer method org.apache.servicecomb.demo.jaxrs.client.SchemeInterfaceJaxrs:reduce "
              + "not exist in contract, microserviceName=jaxrs, schemaId=SchemeInterfaceJaxrs; "
              + "new producer not running or not deployed.",
          e.getMessage());
    }
  }
}
