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

package org.apache.servicecomb.demo.jaxrs.client.pojoDefault;

import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.jaxrs.server.pojoDefault.DefaultResponseModel;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class DefaultModelServiceClient {
  private static RestTemplate template = RestTemplateBuilder.create();

  private static String urlPrefix = "cse://jaxrs/DefaultModelService";

  public static void run() {
    // highway do not support this feature
    CseContext.getInstance().getConsumerProviderManager().setTransport("jaxrs", "rest");
    testDefaultModelService();
  }

  private static void testDefaultModelService() {
    DefaultModel model = new DefaultModel();
    model.setIndex(400);
    DefaultResponseModel result = template.postForObject(urlPrefix + "/model", model, DefaultResponseModel.class);
    TestMgr.check(result.getAge(), 200);
    TestMgr.check(result.getIndex(), 400);
    TestMgr.check(result.getName(), "World");
    TestMgr.check(result.getDesc(), null);
  }
}
