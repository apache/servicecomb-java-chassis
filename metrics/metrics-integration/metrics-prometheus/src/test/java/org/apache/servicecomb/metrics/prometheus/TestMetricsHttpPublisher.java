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

package org.apache.servicecomb.metrics.prometheus;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestMetricsHttpPublisher {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testBadPublishAddress() {
    thrown.expect(ServiceCombException.class);
    new MetricsHttpPublisher("a:b:c");
    Assert.fail("testBadPublishAddress failed,this address must throw ServiceCombException : a:b:c");
  }

  @Test
  public void testBadPublishAddress_BadPort() {
    thrown.expect(ServiceCombException.class);
    new MetricsHttpPublisher("localhost:xxxx");
    Assert.fail("testBadPublishAddress failed,this address must throw ServiceCombException : localhost:xxxx");
  }

  @Test
  public void testBadPublishAddress_ToLargePort() {
    thrown.expect(ServiceCombException.class);
    new MetricsHttpPublisher("localhost:9999999");
    Assert.fail("testBadPublishAddress failed,this address must throw ServiceCombException : localhost:9999999");
  }

  @Test
  public void testRightPublishAddress() {
    new MetricsHttpPublisher("localhost:43234");
  }
}
