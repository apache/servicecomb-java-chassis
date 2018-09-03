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

import static org.junit.Assert.assertEquals;

import org.apache.servicecomb.it.Consumers;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestDefaultValue {
  interface DefaultValueIntf {
    int intQuery(Integer input);

    int intHeader(Integer input);

    int intForm(Integer input);
  }

  interface DefaultValueRequireIntf extends DefaultValueIntf {
    int intQueryRequire(Integer input);

    int intHeaderRequire(Integer input);

    int intFormRequire(Integer input);
  }

  private static Consumers<DefaultValueIntf> consumersJaxrs = new Consumers<>("defaultValueJaxrs",
      DefaultValueIntf.class);

  private static Consumers<DefaultValueRequireIntf> consumersSpringmvc = new Consumers<>("defaultValueSpringmvc",
      DefaultValueRequireIntf.class);

  @BeforeClass
  public static void classSetup() {
    consumersJaxrs.init(ITJUnitUtils.getTransport());
    consumersSpringmvc.init(ITJUnitUtils.getTransport());
  }

  @Test
  public void intQuery_jaxrs_intf() {
    assertEquals(13, consumersJaxrs.getIntf().intQuery(null));
  }

  @Test
  public void intQuery_jaxrs_rt() {
    assertEquals(13, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intQuery", int.class));
  }

  @Test
  public void intHeader_jaxrs_intf() {
    assertEquals(13, consumersJaxrs.getIntf().intHeader(null));
  }

  @Test
  public void intHeader_jaxrs_rt() {
    assertEquals(13, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intHeader", int.class));
  }

  @Test
  public void intForm_jaxrs_intf() {
    assertEquals(13, consumersJaxrs.getIntf().intForm(null));
  }

  @Test
  public void intForm_jaxrs_rt() {
    assertEquals(13, (int) consumersJaxrs.getSCBRestTemplate().postForObject("/intForm", null, int.class));
  }

  @Test
  public void intQuery_springmvc_intf() {
    assertEquals(13, consumersSpringmvc.getIntf().intQuery(null));
  }

  @Test
  public void intQuery_springmvc_rt() {
    assertEquals(13, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intQuery", int.class));
  }

  @Test
  public void intHeader_springmvc_intf() {
    assertEquals(13, consumersSpringmvc.getIntf().intHeader(null));
  }

  @Test
  public void intHeader_springmvc_rt() {
    assertEquals(13, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intHeader", int.class));
  }

  @Test
  public void intForm_springmvc_intf() {
    assertEquals(13, consumersSpringmvc.getIntf().intForm(null));
  }

  @Test
  public void intForm_springmvc_rt() {
    assertEquals(13, (int) consumersSpringmvc.getSCBRestTemplate().postForObject("/intForm", null, int.class));
  }

  @Test
  public void intQuery_require_springmvc_intf() {
    assertEquals(13, consumersSpringmvc.getIntf().intQueryRequire(null));
  }

  @Test
  public void intQuery_require_springmvc_rt() {
    assertEquals(13, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intQueryRequire", int.class));
  }

  @Test
  public void intHeader_require_springmvc_intf() {
    assertEquals(13, consumersSpringmvc.getIntf().intHeaderRequire(null));
  }

  @Test
  public void intHeader_require_springmvc_rt() {
    assertEquals(13, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intHeaderRequire", int.class));
  }

  @Test
  public void intForm_require_springmvc_intf() {
    assertEquals(13, consumersSpringmvc.getIntf().intFormRequire(null));
  }

  @Test
  public void intForm_require_springmvc_rt() {
    assertEquals(13, (int) consumersSpringmvc.getSCBRestTemplate().postForObject("/intFormRequire", null, int.class));
  }
}
