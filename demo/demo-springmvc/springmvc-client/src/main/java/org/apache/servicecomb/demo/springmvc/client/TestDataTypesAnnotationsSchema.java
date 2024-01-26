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
package org.apache.servicecomb.demo.springmvc.client;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class TestDataTypesAnnotationsSchema implements CategorizedTestCase {
  public interface DataTypesAnnotationsItf {
    int[] testIntArrayQuery(int[] param);

    Integer[] testIntegerArrayQuery(Integer[] param);
  }

  @RpcReference(schemaId = "DataTypesAnnotationsSchema", microserviceName = "springmvc")
  private DataTypesAnnotationsItf client;

  @Override
  public void testAllTransport() throws Exception {
    testIntArrayQuery();
    testIntegerArrayQuery();
  }

  private void testIntArrayQuery() {
    int[] request = new int[] {5, 11, 4};
    int[] result = client.testIntArrayQuery(request);
    TestMgr.check(request.length, result.length);
    TestMgr.check(request[1], result[1]);
  }

  private void testIntegerArrayQuery() {
    Integer[] request = new Integer[] {5, 11, 4};
    Integer[] result = client.testIntegerArrayQuery(request);
    TestMgr.check(request.length, result.length);
    TestMgr.check(request[1], result[1]);
  }
}
