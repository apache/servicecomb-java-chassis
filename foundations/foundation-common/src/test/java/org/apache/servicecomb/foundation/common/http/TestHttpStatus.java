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

package org.apache.servicecomb.foundation.common.http;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.junit.Assert;
import org.junit.Test;


public class TestHttpStatus {
  @Test
  public void testIsSuccessCode() {
    Assert.assertTrue(HttpStatus.isSuccess(200));
    Assert.assertFalse(HttpStatus.isSuccess(300));
  }

  @Test
  public void testIsSuccessType() {
    Assert.assertTrue(HttpStatus.isSuccess(Status.OK));
    Assert.assertFalse(HttpStatus.isSuccess(Status.BAD_REQUEST));
  }

  @Test
  public void testGetStatusCode() {
    HttpStatus status = new HttpStatus(200, "ok");
    Assert.assertEquals(200, status.getStatusCode());
  }

  @Test
  public void testGetFamily() {
    HttpStatus status = new HttpStatus(200, "ok");
    Assert.assertEquals(Family.familyOf(200), status.getFamily());
  }

  @Test
  public void testGetReasonPhrase() {
    HttpStatus status = new HttpStatus(200, "ok");
    Assert.assertEquals("ok", status.getReasonPhrase());
  }
}
