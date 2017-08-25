/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.springmvc.reference;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import io.servicecomb.swagger.invocation.Response;

public class TestCseClientHttpResponse {
  private Object result;

  Response response = Response.ok(result);

  CseClientHttpResponse cseclientrequest = new CseClientHttpResponse(response);

  @Test
  public void testGetResult() {
    Assert.assertNull(cseclientrequest.getResult());
  }

  @Test
  public void testGetBody() throws IOException {
    Assert.assertNotNull(cseclientrequest.getBody());
  }

  @Test
  public void testGetHeaders() {
    Assert.assertNotNull(cseclientrequest.getHeaders());
  }

  @Test
  public void testGetStatusCode() throws IOException {
    Assert.assertEquals(HttpStatus.OK, cseclientrequest.getStatusCode());
  }

  @Test
  public void testGetRawStatusCode() throws IOException {
    Assert.assertEquals(200, cseclientrequest.getRawStatusCode());
  }

  @Test
  public void testGetStatusText() throws IOException {
    cseclientrequest.close();
    Assert.assertEquals(HttpStatus.OK.getReasonPhrase(), cseclientrequest.getStatusText());
  }
}
