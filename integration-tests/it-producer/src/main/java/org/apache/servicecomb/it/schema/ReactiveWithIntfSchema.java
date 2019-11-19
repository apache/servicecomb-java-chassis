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

import java.util.concurrent.CompletableFuture;

import org.apache.servicecomb.provider.pojo.RpcSchema;

/**
 * verify https://issues.apache.org/jira/browse/SCB-858
 */
@RpcSchema(schemaId = "reactiveWithIntf")
public class ReactiveWithIntfSchema implements ReactiveHelloIntf {//}, BootListener {
//  @ApiOperation(value = "", hidden = true)
//  @Override
//  public void onAfterProducerProvider(BootEvent event) {
//    Microservice microservice = RegistryUtils.getMicroservice();
//    MicroserviceMeta microserviceMeta = SCBEngine.getInstance().getProducerMicroserviceMeta();
//    SchemaMeta schemaMeta = microserviceMeta.findSchemaMeta("reactiveWithIntf");
//    schemaMeta.getSwagger().getInfo()
//        .setVendorExtension(SwaggerConst.EXT_JAVA_INTF, ReactiveHelloIntf.class.getName());
//    String content = SwaggerUtils.swaggerToString(schemaMeta.getSwagger());
//    microservice.getSchemaMap().put(schemaMeta.getSchemaId(), content);
//  }

  @Override
  public CompletableFuture<String> hello(String name) {
    return CompletableFuture.completedFuture("hello " + name);
  }
}
