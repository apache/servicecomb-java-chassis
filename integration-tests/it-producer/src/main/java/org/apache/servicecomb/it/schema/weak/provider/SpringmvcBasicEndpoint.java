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

package org.apache.servicecomb.it.schema.weak.provider;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestSchema(schemaId = "SpringmvcBasicEndpoint")
@RequestMapping(path = "/springmvc/basic", produces = MediaType.APPLICATION_JSON)
public class SpringmvcBasicEndpoint {
  @RequestMapping(path = "/postObject", method = RequestMethod.POST)
  public SpringmvcBasicResponseModel postObject(@RequestBody SpringmvcBasicRequestModel requestModel) {
    SpringmvcBasicResponseModel model = new SpringmvcBasicResponseModel();
    model.setResponseId(requestModel.getRequestId());
    model.setResultMessage(requestModel.getName());
    return model;
  }

  @RequestMapping(path = "/postListObject", method = RequestMethod.POST)
  public List<SpringmvcBasicResponseModel> postListObject(@RequestBody SpringmvcBasicRequestModel requestModel) {
    SpringmvcBasicResponseModel model = new SpringmvcBasicResponseModel();
    model.setResponseId(requestModel.getRequestId());
    model.setResultMessage(requestModel.getName());
    List<SpringmvcBasicResponseModel> result = new ArrayList<>();
    result.add(model);
    return result;
  }
}
