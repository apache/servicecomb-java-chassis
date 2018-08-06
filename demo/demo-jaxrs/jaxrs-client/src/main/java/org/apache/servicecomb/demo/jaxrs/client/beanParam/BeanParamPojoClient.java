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

package org.apache.servicecomb.demo.jaxrs.client.beanParam;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.provider.pojo.Invoker;

public class BeanParamPojoClient {
  private BeanParamTestServiceIntf beanParamTestServiceIntf;

  public BeanParamPojoClient() {
    beanParamTestServiceIntf = Invoker.createProxy("jaxrs", "beanParamTest", BeanParamTestServiceIntf.class);
  }

  public void testAll() {
    testBeanParam();
    testUpload();
  }

  private void testBeanParam() {
    String result = beanParamTestServiceIntf.beanParameterTest("querySwaggerValue", 2, "pathSwaggerValue", 10, "extra");
    TestMgr.check(
        "invocationContextConsistency=true|testBeanParameter=TestBeanParameter{queryStr='querySwaggerValue', headerInt=2, "
            + "pathStr='pathSwaggerValue', cookieLong=10}|extraQuery=extra",
        result);
  }

  private void testUpload() {
    BufferedInputStream bufferedInputStream0 = new BufferedInputStream(new ByteArrayInputStream("up0".getBytes()));
    BufferedInputStream bufferedInputStream1 = new BufferedInputStream(new ByteArrayInputStream("up1".getBytes()));
    BufferedInputStream bufferedInputStream2 = new BufferedInputStream(new ByteArrayInputStream("up2".getBytes()));

    String result = beanParamTestServiceIntf.beanParameterTestUpload(
        bufferedInputStream0, "queryTest", bufferedInputStream1, bufferedInputStream2, "ex");
    TestMgr.check(
        "testBeanParameter=TestBeanParameterWithUpload{queryStr='queryTest'}|extraQuery=ex|up0=up0|up1=up1|up2=up2",
        result);
  }
}
