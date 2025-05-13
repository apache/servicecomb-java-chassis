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

import jakarta.ws.rs.core.MediaType;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestSchema(schemaId = "Compatible1xTestSchema")
@RequestMapping(path = "/compatible", produces = MediaType.APPLICATION_JSON)
public class Compatible1xTestSchema {
  @GetMapping(path = "/parameterName")
  public String parameterName(@RequestParam(name = "a", defaultValue = "10") int a,
      @RequestParam(name = "b", defaultValue = "10") int b) {
    return ContextUtils.getInvocationContext().getContext(Const.SRC_MICROSERVICE) + a + b * 2;
  }

  @GetMapping(path = "/parameterNameServerContext")
  public String parameterNameServerContext(InvocationContext context,
      @RequestParam(name = "a", defaultValue = "10") int a,
      @RequestParam(name = "b", defaultValue = "10") int b) {
    return context.getContext(Const.SRC_MICROSERVICE) + a + b * 2;
  }

  @GetMapping(path = "/beanParameter")
  public String beanParameter(CompatibleQueryBean bean) {
    return bean.getName() + bean.getAge();
  }
}
