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

package org.apache.servicecomb.it.testcase;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Strings;

public class TestRestServerConfig {
  // GET /v1/restServerConfig/testMaxInitialLineLength?q=...... HTTP/1.1
  private static final String INITIAL_LINE_SUFFIX = " HTTP/1.1";

  private static final String INITIAL_LINE_PREFIX = "GET /v1/restServerConfig/testMaxInitialLineLength?q=";

  interface RestServerConfigSchemaIntf {
    String testMaxInitialLineLength(String q);
  }

  static Consumers<RestServerConfigSchemaIntf> consumers = new Consumers<>("restServerConfig",
      RestServerConfigSchemaIntf.class);

  /**
   * Max initial line length is set to 5000
   */
  @Test
  public void testMaxInitialLineLength5000() {
    String q = Strings.repeat("q", 5000 - INITIAL_LINE_PREFIX.length() - INITIAL_LINE_SUFFIX.length());
    String result = consumers.getIntf().testMaxInitialLineLength(q);
    Assert.assertEquals("OK", result);
  }

  @Test
  public void testMaxInitialLineLength5001() {
    String q = Strings.repeat("q", 5001 - INITIAL_LINE_PREFIX.length() - INITIAL_LINE_SUFFIX.length());
    try {
      consumers.getIntf().testMaxInitialLineLength(q);
      Assert.fail("an exception is expected!");
    } catch (InvocationException e) {
      Assert.assertEquals(414, e.getStatusCode());
    }
  }
}
