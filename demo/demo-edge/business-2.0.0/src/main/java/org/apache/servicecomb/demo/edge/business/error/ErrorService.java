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

package org.apache.servicecomb.demo.edge.business.error;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestSchema(schemaId = "error-v2")
@RequestMapping(path = "/business/v2/error")
public class ErrorService {
  @RequestMapping(path = "/add", method = RequestMethod.GET)
  public int add(int x, int y) {
    if (x == 99) {
      throw new NullPointerException("un expected NPE test.");
    }
    if (x == 88) {
      ErrorData data = new ErrorData();
      data.setId(12);
      data.setMessage("not allowed id.");
      throw new InvocationException(Status.FORBIDDEN, data);
    }
    if (x == 77) {
      throw new IllegalStateException("77");
    }
    return x + y;
  }
}
