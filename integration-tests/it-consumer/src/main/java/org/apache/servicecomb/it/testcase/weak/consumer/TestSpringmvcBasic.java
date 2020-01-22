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

package org.apache.servicecomb.it.testcase.weak.consumer;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.it.Consumers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

public class TestSpringmvcBasic {
  static Consumers<SpringmvcBasicService> consumers =
      new Consumers<>("SpringmvcBasicEndpoint", SpringmvcBasicService.class);

  @Test
  @SuppressWarnings("unchecked")
  public void testWeakTypeInvoke() {
    SpringmvcBasicRequestModel requestModel = new SpringmvcBasicRequestModel();
    requestModel.setName("Hello World");
    SpringmvcBasicResponseModel responseModel;
    List<SpringmvcBasicResponseModel> responseModelList;

    // Invoke a spring mvc provider using RPC
    responseModel = consumers.getIntf().postObject(requestModel);
    Assert.assertEquals("Hello World", responseModel.getResultMessage());
    responseModelList = consumers.getIntf().postListObject(requestModel);
    Assert.assertEquals("Hello World", responseModelList.get(0).getResultMessage());

    // Invoke using restTemplate
    responseModel = consumers.getSCBRestTemplate()
        .postForObject("/postObject", requestModel, SpringmvcBasicResponseModel.class);
    Assert.assertEquals("Hello World", responseModel.getResultMessage());
    // 2.x recommended usage
    HttpEntity<SpringmvcBasicRequestModel> requestEntity = new HttpEntity<>(requestModel, null);
    responseModelList = consumers.getSCBRestTemplate().exchange("/postListObject", HttpMethod.POST, requestEntity,
        new ParameterizedTypeReference<List<SpringmvcBasicResponseModel>>() {
        }).getBody();
    Assert.assertEquals("Hello World", responseModelList.get(0).getResultMessage());
    // obj should be a map. For 1.x compatibility, if want get  List<SpringmvcBasicResponseModel>, SpringmvcBasicResponseModel should be defined in client. Other
    // test case has covered this situation.
    List<Map<String, Object>> obj = consumers.getSCBRestTemplate()
        .postForObject("/postListObject", requestModel, List.class);
    Assert.assertEquals("Hello World", obj.get(0).get("resultMessage"));
  }
}
