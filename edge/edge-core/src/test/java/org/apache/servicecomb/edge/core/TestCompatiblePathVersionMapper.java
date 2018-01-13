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

package org.apache.servicecomb.edge.core;

import org.apache.servicecomb.foundation.common.exceptions.ServiceCombException;
import org.apache.servicecomb.serviceregistry.version.VersionRule;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestCompatiblePathVersionMapper {
  CompatiblePathVersionMapper mapper = new CompatiblePathVersionMapper();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void getOrCreate() {
    VersionRule versionRule = mapper.getOrCreate("v1");

    Assert.assertEquals("1.0.0-2.0.0", versionRule.getVersionRule());
  }

  @Test
  public void createVersionRule_empty() {
    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage(Matchers.is("pathVersion \"\" is invalid, format must be v+number or V+number."));

    mapper.getOrCreate("");
  }

  @Test
  public void createVersionRule_invalidFormat() {
    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage(Matchers.is("pathVersion \"a1\" is invalid, format must be v+number or V+number."));

    mapper.getOrCreate("a1");
  }

  @Test
  public void createVersionRule_invalidNumber() {
    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage(Matchers.is("pathVersion \"va\" is invalid, format must be v+number or V+number."));

    mapper.getOrCreate("va");
  }

  @Test
  public void createVersionRule_tooSmall() {
    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage(Matchers.is("pathVersion \"v-1\" is invalid, version range is [0, 32767]."));

    mapper.getOrCreate("v-1");
  }

  @Test
  public void createVersionRule_tooBig() {
    expectedException.expect(ServiceCombException.class);
    expectedException.expectMessage(Matchers.is("pathVersion \"v32768\" is invalid, version range is [0, 32767]."));

    mapper.getOrCreate("v32768");
  }

  @Test
  public void createVersionRule_32767() {
    VersionRule versionRule = mapper.getOrCreate("v32767");

    Assert.assertEquals("32767.0.0+", versionRule.getVersionRule());
  }
}
