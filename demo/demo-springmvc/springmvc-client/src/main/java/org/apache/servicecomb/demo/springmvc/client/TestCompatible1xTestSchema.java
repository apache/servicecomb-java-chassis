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
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.stereotype.Component;

@Component
public class TestCompatible1xTestSchema implements CategorizedTestCase {
  @RpcReference(microserviceName = "springmvc", schemaId = "Compatible1xTestSchema")
  private ICompatible1xTestSchema schema;

  @Override
  public void testAllTransport() throws Exception {
    testParameterName();
    testParameterNameClientContext();
    testParameterNameServerContext();
    testBeanParameter();
    testParameterNamePartMatchLeft();
    testParameterNamePartMatchRight();
  }

  private void testParameterNamePartMatchRight() {
    String result = schema.parameterNamePartMatchRight(3, 4);
    TestMgr.check("springmvcClient38", result);
  }

  private void testParameterNamePartMatchLeft() {
    String result = schema.parameterNamePartMatchLeft(3, 4);
    TestMgr.check("springmvcClient38", result);
  }

  private void testBeanParameter() {
    String result = schema.beanParameter("name", 4);
    TestMgr.check("name4", result);
  }

  private void testParameterNameServerContext() {
    String result = schema.parameterNameServerContext(3, 4);
    TestMgr.check("springmvcClient38", result);
  }

  private void testParameterNameClientContext() {
    String result = schema.parameterName(new InvocationContext(), 3, 4);
    TestMgr.check("springmvcClient38", result);
  }

  private void testParameterName() {
    String result = schema.parameterName(3, 4);
    TestMgr.check("springmvcClient38", result);
  }
}
