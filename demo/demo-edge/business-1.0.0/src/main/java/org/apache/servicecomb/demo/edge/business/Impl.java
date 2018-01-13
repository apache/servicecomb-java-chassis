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

package org.apache.servicecomb.demo.edge.business;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.demo.edge.model.AppClientDataRsp;
import org.apache.servicecomb.demo.edge.model.ChannelRequestBase;
import org.apache.servicecomb.demo.edge.model.ResultWithInstance;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestSchema(schemaId = "news-v1")
@RequestMapping(path = "/business/v1")
public class Impl {
  @RequestMapping(path = "/channel/news/subscribe", method = RequestMethod.POST)
  public AppClientDataRsp subscribeNewsColumn(@RequestBody ChannelRequestBase request) {
    AppClientDataRsp response = new AppClientDataRsp();
    String rsp = StringUtils.rightPad("edge test", 1024, "*");
    response.setRsp(rsp);
    return response;
  }

  @RequestMapping(path = "/add", method = RequestMethod.GET)
  public ResultWithInstance add(int x, int y) {
    return ResultWithInstance.create(x + y);
  }
}
