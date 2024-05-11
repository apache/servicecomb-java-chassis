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

import java.util.Date;

import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.springmvc.client.ThirdSvc.ThirdSvcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TestThirdSvc implements CategorizedTestCase {
  private ThirdSvcClient client;

  @Autowired
  public void setThirdSvcClient(ThirdSvcClient client) {
    this.client = client;
  }

  @Override
  public void testAllTransport() throws Exception {
    testResponseEntity();

    testDifferentParameterName();
  }

  private void testDifferentParameterName() {
    String result = client.getAuthorization("test", "param", "auth");
    TestMgr.check("testparamauth", result);
  }

  private void testResponseEntity() {
    Date date = new Date();
    ResponseEntity<Date> responseEntity = client.responseEntity(date);
    TestMgr.check(date, responseEntity.getBody());
  }
}
