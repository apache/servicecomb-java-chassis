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

import io.swagger.annotations.SwaggerDefinition;

@Component
public class TestSameService implements CategorizedTestCase {
  @SwaggerDefinition(basePath = "/SameInterface1")
  public interface SameInterface1 {
    public String sayHello(String name);
  }

  @SwaggerDefinition(basePath = "/SameInterface2")
  public interface SameInterface2 {
    public String sayHello(String name);
  }

  @SwaggerDefinition(basePath = "/SameService1")
  public interface SameService1 {
    public String sayHello(String name);
  }

  @SwaggerDefinition(basePath = "/SameService2")
  public interface SameService2 {
    public String sayHello(String name);
  }

  @RpcReference(microserviceName = "pojo", schemaId = "SameService1")
  SameService1 sameService1;

  @RpcReference(microserviceName = "pojo", schemaId = "SameService2")
  SameService2 sameService2;

  @RpcReference(microserviceName = "pojo", schemaId = "SameInterface1")
  SameInterface1 sameInterface1;

  @RpcReference(microserviceName = "pojo", schemaId = "SameInterface2")
  SameInterface2 sameInterface2;

  @Override
  public void testAllTransport() throws Exception {
    TestMgr.check("pk1-svc-1", sameService1.sayHello("1"));
    TestMgr.check("pk2-svc-1", sameService2.sayHello("1"));
    TestMgr.check("pk1-inf-1", sameInterface1.sayHello("1"));
    TestMgr.check("pk2-inf-1", sameInterface2.sayHello("1"));
  }
}
