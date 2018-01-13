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

import javax.ws.rs.core.Response.StatusType;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestHttpStatusUtils {
  HttpStatusManager mgr = new HttpStatusManager();

  StatusType st;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testStandard() {
    st = HttpStatusUtils.getOrCreateByStatusCode(200);
    Assert.assertEquals(200, st.getStatusCode());
  }

  @Test
  public void testNotStandard() {
    st = mgr.getOrCreateByStatusCode(250);
    Assert.assertEquals(250, st.getStatusCode());
  }

  @Test
  public void testAddRepeat() {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(Matchers.is("repeated status code: 200"));

    mgr.addStatusType(new HttpStatus(200, "ok"));
  }
}
