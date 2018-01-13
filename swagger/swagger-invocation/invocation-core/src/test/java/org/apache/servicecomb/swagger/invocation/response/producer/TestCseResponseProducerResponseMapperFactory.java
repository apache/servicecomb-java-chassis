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
package org.apache.servicecomb.swagger.invocation.response.producer;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.swagger.invocation.Response;
import org.junit.Assert;
import org.junit.Test;

public class TestCseResponseProducerResponseMapperFactory {
  CseResponseProducerResponseMapperFactory factory = new CseResponseProducerResponseMapperFactory();

  @Test
  public void isMatch_true() {
    Assert.assertTrue(factory.isMatch(String.class, Response.class));
  }

  @Test
  public void isMatch_false() {
    Assert.assertFalse(factory.isMatch(String.class, String.class));
  }

  @Test
  public void createResponseMapper() {
    ProducerResponseMapper mapper = factory.createResponseMapper(null, null, Response.class);
    Response response = Response.ok(null);

    Assert.assertSame(response, mapper.mapResponse(Status.OK, response));
  }
}
