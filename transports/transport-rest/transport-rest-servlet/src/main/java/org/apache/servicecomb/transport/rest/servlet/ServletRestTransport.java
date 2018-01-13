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

package org.apache.servicecomb.transport.rest.servlet;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.serviceregistry.api.Const;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.transport.rest.client.RestTransportClient;
import org.apache.servicecomb.transport.rest.client.RestTransportClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ServletRestTransport extends AbstractTransport {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServletRestTransport.class);

  private RestTransportClient restClient;

  @Override
  public String getName() {
    return org.apache.servicecomb.core.Const.RESTFUL;
  }

  @Override
  public boolean canInit() {
    String listenAddress = ServletConfig.getLocalServerAddress();
    if (listenAddress == null) {
      // not publish, but can init and be RESTful client
      return true;
    }

    if (!ServletUtils.canPublishEndpoint(listenAddress)) {
      LOGGER.info("ignore transport {}.", this.getClass().getName());
      return false;
    }

    return true;
  }

  @Override
  public boolean init() {
    String urlPrefix = System.getProperty(Const.URL_PREFIX);
    Map<String, String> queryMap = new HashMap<>();
    if (!StringUtils.isEmpty(urlPrefix)) {
      queryMap.put(Const.URL_PREFIX, urlPrefix);
    }

    String listenAddress = ServletConfig.getLocalServerAddress();
    setListenAddressWithoutSchema(listenAddress, queryMap);

    restClient = RestTransportClientManager.INSTANCE.getRestClient();
    return true;
  }

  @Override
  public void send(Invocation invocation, AsyncResponse asyncResp) throws Exception {
    restClient.send(invocation, asyncResp);
  }
}
