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
package org.apache.servicecomb.it.edge.encrypt.filter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.it.authentication.encrypt.Hcr;
import org.apache.servicecomb.it.edge.EdgeConst;
import org.apache.servicecomb.it.edge.encrypt.EncryptContext;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class EdgeSignatureRequestFilter implements HttpServerFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(EdgeSignatureRequestFilter.class);

  @Override
  public int getOrder() {
    return -10000;
  }

  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    EncryptContext encryptContext = (EncryptContext) invocation.getHandlerContext().get(EdgeConst.ENCRYPT_CONTEXT);
    if (encryptContext == null) {
      return null;
    }
    Hcr hcr = encryptContext.getHcr();

    // signature for query and form
    List<String> names = Collections.list(requestEx.getParameterNames());
    names.sort(Comparator.naturalOrder());

    Hasher hasher = Hashing.sha256().newHasher();
    hasher.putString(hcr.getSignatureKey(), StandardCharsets.UTF_8);
    for (String name : names) {
      hasher.putString(name, StandardCharsets.UTF_8);
      hasher.putString(requestEx.getParameter(name), StandardCharsets.UTF_8);
    }
    LOGGER.info("afterReceiveRequest signature: {}", hasher.hash().toString());

    return null;
  }
}
