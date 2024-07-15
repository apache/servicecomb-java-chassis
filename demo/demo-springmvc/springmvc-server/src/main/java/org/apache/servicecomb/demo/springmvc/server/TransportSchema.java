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

package org.apache.servicecomb.demo.springmvc.server;

import org.apache.servicecomb.core.CoreConst;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.annotation.Transport;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "TransportSchema")
@RequestMapping(path = "/transport")
public class TransportSchema {
  @GetMapping(path = "/restTransport")
  @Transport(name = CoreConst.RESTFUL)
  public boolean restTransport(InvocationContext invocation) {
    return CoreConst.RESTFUL.equals(((Invocation) invocation).getTransportName());
  }

  @GetMapping(path = "/highwayTransport")
  @Transport(name = CoreConst.HIGHWAY)
  public boolean highwayTransport(InvocationContext invocation) {
    return CoreConst.HIGHWAY.equals(((Invocation) invocation).getTransportName());
  }
}
