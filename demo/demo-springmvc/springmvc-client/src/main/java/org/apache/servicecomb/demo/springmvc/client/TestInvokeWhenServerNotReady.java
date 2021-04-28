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

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.demo.CommonSchemaInterface;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TestInvokeWhenServerNotReady {
  @RpcReference(schemaId = "SpringMVCCommonSchemaInterface", microserviceName = "springmvc")
  private CommonSchemaInterface client;

  // only invoke RPC before system is up, these setup is for testing calls before system ready
  public TestInvokeWhenServerNotReady() {
    startRestTemplateCall();
    startRpcCall();
    startInvokerUtilsCall();
  }

  private void startRestTemplateCall() {
    new Thread() {
      public void run() {
        for (int i = 0; i < 100; i++) {
          try {
            RestTemplate template = RestTemplateBuilder.create();
            template.getForObject("servicecomb://springmvc/upload/isServerStartUpSuccess", Boolean.class);
          } catch (Throwable e) {
            // ignore
          }
        }
      }
    }.start();
  }

  private void startRpcCall() {
    new Thread() {
      public void run() {
        for (int i = 0; i < 100; i++) {
          try {
            InvocationContext context = new InvocationContext();
            client.testInvocationTimeout(context, 1001, "customized");
          } catch (Throwable e) {
            // ignore
          }
        }
      }
    }.start();
  }

  private void startInvokerUtilsCall() {
    new Thread() {
      public void run() {
        for (int i = 0; i < 100; i++) {
          try {
            Map<String, Object> args = new HashMap<>();
            args.put("timeout", 1);
            args.put("name", "customized");
            InvokerUtils
                .syncInvoke("springmvc", "SpringMVCCommonSchemaInterface", "testInvocationTimeout", args, String.class);
          } catch (Throwable e) {
            // ignore
          }
        }
      }
    }.start();
  }
}
