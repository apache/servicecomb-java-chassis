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

package org.apache.servicecomb.demo.pojo.client;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class TestSchemeInterface implements CategorizedTestCase {
  @RpcReference(microserviceName = "pojo", schemaId = "SchemaInterface")
  private SchemaInterface pojo;

  @Override
  public void testAllTransport() throws Exception {
    TestMgr.check("hello", pojo.echo("hello"));

    try {
      pojo.echoError("hello");
      TestMgr.failed("should throw exception", new Exception());
    } catch (Exception e) {
      TestMgr.check(
          "Consumer method org.apache.servicecomb.demo.pojo.client.SchemaInterface:"
              + "echoError not exist in contract, microserviceName=pojo, "
              + "schemaId=SchemaInterface; new producer not running or not deployed.",
          e.getMessage());
    }
  }
}
