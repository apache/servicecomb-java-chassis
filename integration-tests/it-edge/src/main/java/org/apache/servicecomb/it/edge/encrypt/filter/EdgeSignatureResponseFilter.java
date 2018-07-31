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

import org.apache.servicecomb.common.rest.filter.HttpServerFilter;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.foundation.vertx.http.HttpServletRequestEx;
import org.apache.servicecomb.foundation.vertx.http.HttpServletResponseEx;
import org.apache.servicecomb.it.authentication.encrypt.Hcr;
import org.apache.servicecomb.it.edge.EdgeConst;
import org.apache.servicecomb.it.edge.encrypt.EncryptContext;
import org.apache.servicecomb.swagger.invocation.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import io.vertx.core.buffer.Buffer;

public class EdgeSignatureResponseFilter implements HttpServerFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(EdgeSignatureResponseFilter.class);

  @Override
  public int getOrder() {
    return 10000;
  }

  @Override
  public Response afterReceiveRequest(Invocation invocation, HttpServletRequestEx requestEx) {
    return null;
  }

  @Override
  public void beforeSendResponse(Invocation invocation, HttpServletResponseEx responseEx) {
    if (invocation == null) {
      return;
    }

    EncryptContext encryptContext = (EncryptContext) invocation.getHandlerContext().get(EdgeConst.ENCRYPT_CONTEXT);
    if (encryptContext == null) {
      return;
    }
    Hcr hcr = encryptContext.getHcr();

    // bad practice: it's better to set signature in response header
    Buffer bodyBuffer = responseEx.getBodyBuffer();
    String body = bodyBuffer.toString();
    if (body.endsWith("}")) {
      Hasher hasher = Hashing.sha256().newHasher();
      hasher.putString(hcr.getSignatureKey(), StandardCharsets.UTF_8);
      hasher.putString(body, StandardCharsets.UTF_8);
      String signature = hasher.hash().toString();
      LOGGER.info("beforeSendResponse signature: {}", signature);

      body = body.substring(0, body.length() - 1) + ",\"signature\":\"" + signature + "\"}";
      responseEx.setBodyBuffer(Buffer.buffer(body));
    }
  }
}
