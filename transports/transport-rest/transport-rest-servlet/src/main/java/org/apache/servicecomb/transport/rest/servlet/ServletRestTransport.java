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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.transport.AbstractTransport;
import org.apache.servicecomb.foundation.common.utils.ClassLoaderScopeContext;
import org.apache.servicecomb.registry.definition.DefinitionConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletRestTransport extends AbstractTransport {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServletRestTransport.class);

  @Override
  public String getName() {
    return CoreConst.RESTFUL;
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
    String urlPrefix = ClassLoaderScopeContext.getClassLoaderScopeProperty(DefinitionConst.URL_PREFIX);
    Map<String, String> queryMap = new HashMap<>();
    if (!StringUtils.isEmpty(urlPrefix)) {
      queryMap.put(DefinitionConst.URL_PREFIX, urlPrefix);
    }

    String listenAddress = ServletConfig.getLocalServerAddress();
    setListenAddressWithoutSchema(listenAddress, queryMap);

    return true;
  }
}
