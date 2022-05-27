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

package org.apache.servicecomb.demo.localRegistryClient;

import static org.hamcrest.core.Is.is;

import org.apache.servicecomb.demo.TestMgr;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

public class LocalRegistryIT {
  @Before
  public void setUp() throws Exception {
    TestMgr.errors().clear();
  }

  @Test
  public void clientGetsNoError() throws Exception {
    Application.main(new String[0]);

    MatcherAssert.assertThat(TestMgr.errors().isEmpty(), is(true));
  }
}
