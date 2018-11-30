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
package org.apache.servicecomb.it.schema;

import static org.apache.servicecomb.foundation.common.base.ServiceCombConstants.CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.netflix.config.DynamicPropertyFactory;

@RestSchema(schemaId = "trace")
@RequestMapping(path = "/v1/trace")
public class TraceSchema {
  interface TraceSchemaIntf {
    CompletableFuture<String> echo();
  }

  TraceSchemaIntf intf = Invoker.createProxy(
      DynamicPropertyFactory.getInstance().getStringProperty(CONFIG_QUALIFIED_MICROSERVICE_NAME_KEY, null).get(),
      "trace",
      TraceSchemaIntf.class);

  @GetMapping(path = "echo")
  public String echo(InvocationContext context) {
    return context.getContext(Const.TRACE_ID_NAME);
  }

  @GetMapping(path = "echo-proxy")
  public String echoProxy() throws ExecutionException, InterruptedException {
    return intf.echo().get();
  }
}
