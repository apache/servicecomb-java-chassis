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

package org.apache.servicecomb.governance.handler.ext;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = "classpath:META-INF/spring/*.xml", initializers = ConfigDataApplicationContextInitializer.class)
public class RetryExtensionTest {

  @Test
  public void test_status_code_to_contains() {
    List<String> statusList = Arrays.asList("502", "503");
    boolean result = AbstractRetryExtension.statusCodeContains(statusList, "502");
    Assert.assertTrue(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "504");
    Assert.assertFalse(result);

    statusList = Arrays.asList("5xx", "4x4", "4x", "x32", "xx6");
    result = AbstractRetryExtension.statusCodeContains(statusList, "502");
    Assert.assertTrue(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "504");
    Assert.assertTrue(result);

    statusList = Arrays.asList("4x4", "x32", "xx6");
    result = AbstractRetryExtension.statusCodeContains(statusList, "402");
    Assert.assertFalse(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "404");
    Assert.assertTrue(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "332");
    Assert.assertTrue(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "446");
    Assert.assertTrue(result);

    statusList = Arrays.asList("4x", "x3x", "x5");
    result = AbstractRetryExtension.statusCodeContains(statusList, "446");
    Assert.assertFalse(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "455");
    Assert.assertFalse(result);

    result = AbstractRetryExtension.statusCodeContains(statusList, "434");
    Assert.assertTrue(result);
  }
}
