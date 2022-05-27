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

package org.apache.servicecomb.demo.springmvc.server;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.demo.CommonSchemaInterface;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

@RestSchema(schemaId = "SpringMVCCommonSchemaInterface", schemaInterface = CommonSchemaInterface.class)
public class SpringMVCCommonSchemaInterface implements CommonSchemaInterface {
  @Override
  public String testInvocationTimeout(long timeout, String name) {
    try {
      Thread.sleep(timeout);
    } catch (InterruptedException e) {

    }

    return name;
  }

  @Override
  public String testInvocationTimeout(InvocationContext context, long timeout,
      String name) {

    if ("customized".equals(name)) {
      try {
        Thread.sleep(timeout);
      } catch (InterruptedException e) {

      }

      Invocation invocation = (Invocation) context;
      invocation.ensureInvocationNotTimeout();

      throw new InvocationException(Status.BAD_REQUEST, "not expected result");
    }

    return testInvocationTimeout(timeout, name);
  }

  @Override
  public String testInvocationTimeoutInClientWait(long timeout, String name) {
    return testInvocationTimeout(timeout, name);
  }
}
