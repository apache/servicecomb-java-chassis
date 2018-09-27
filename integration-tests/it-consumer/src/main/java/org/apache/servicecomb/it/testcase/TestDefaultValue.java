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
import org.junit.Test;

public class TestDefaultValue {
  interface DefaultValueIntf {
    int intQuery(Integer input);

    int intHeader(Integer input);

    int intForm(Integer input);

    String stringQuery(String input);

    String stringHeader(String input);

    String stringForm(String input);

    double doubleQuery(Double input);

    double doubleHeader(Double input);

    double doubleForm(Double input);

    // float
    float floatQuery(Float input);

    float floatHeader(Float input);

    float floatForm(Float input);
  }

  interface DefaultValueRequireIntf extends DefaultValueIntf {
    int intQueryRequire(Integer input);

    int intHeaderRequire(Integer input);

    int intFormRequire(Integer input);

    String stringQueryRequire(String input);

    String stringHeaderRequire(String input);

    String stringFormRequire(String input);

    double doubleQueryRequire(Double input);

    double doubleHeaderRequire(Double input);

    double doubleFormRequire(Double input);

    float floatQueryRequire(Float input);

    float floatHeaderRequire(Float input);

    float floatFormRequire(Float input);
  }

  private String defaultStr = "string";

  private int defaultInt = 13;

  private double defaultDouble = 10.2;

  private float defaultFloat = 10.2f;

  private static Consumers<DefaultValueIntf> consumersJaxrs = new Consumers<>("defaultValueJaxrs",
      DefaultValueIntf.class);

  private static Consumers<DefaultValueRequireIntf> consumersSpringmvc = new Consumers<>("defaultValueSpringmvc",
      DefaultValueRequireIntf.class);

  @Test
  public void intQuery_jaxrs_intf() {
    assertEquals(defaultInt, consumersJaxrs.getIntf().intQuery(null));
  }

  @Test
  public void doubleQuery_jaxrs_intf() {
    assertEquals(defaultDouble, consumersJaxrs.getIntf().doubleQuery(null), 0.0);
  }

  @Test
  public void stringQuery_jaxrs_intf() {
    assertEquals(defaultStr, consumersJaxrs.getIntf().stringQuery(null));
  }

  @Test
  public void intQuery_jaxrs_rt() {
    assertEquals(defaultInt, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intQuery", int.class));
  }

  @Test
  public void doubleQuery_jaxrs_rt() {
    assertEquals(defaultDouble, consumersJaxrs.getSCBRestTemplate().getForObject("/doubleQuery", double.class),
        0.0);
  }

  @Test
  public void stringQuery_jaxrs_rt() {
    assertEquals(defaultStr, consumersJaxrs.getSCBRestTemplate().getForObject("/stringQuery", String.class));
  }

  @Test
  public void intHeader_jaxrs_intf() {
    assertEquals(defaultInt, consumersJaxrs.getIntf().intHeader(null));
  }

  @Test
  public void doubleHeader_jaxrs_intf() {
    assertEquals(defaultDouble, consumersJaxrs.getIntf().doubleHeader(null), 0.0);
  }

  @Test
  public void stringHeader_jaxrs_intf() {
    assertEquals(defaultStr, consumersJaxrs.getIntf().stringHeader(null));
  }

  @Test
  public void intHeader_jaxrs_rt() {
    assertEquals(defaultInt, (int) consumersJaxrs.getSCBRestTemplate().getForObject("/intHeader", int.class));
  }

  @Test
  public void doubleHeader_jaxrs_rt() {
    assertEquals(defaultDouble,
        consumersJaxrs.getSCBRestTemplate().getForObject("/doubleHeader", double.class), 0.0);
  }

  @Test
  public void stringHeader_jaxrs_rt() {
    assertEquals(defaultStr,
        consumersJaxrs.getSCBRestTemplate().getForObject("/stringHeader", String.class));
  }

  @Test
  public void intForm_jaxrs_intf() {
    assertEquals(defaultInt, consumersJaxrs.getIntf().intForm(null));
  }

  @Test
  public void doubleForm_jaxrs_intf() {
    assertEquals(defaultDouble, consumersJaxrs.getIntf().doubleForm(null), 0.0);
  }

  @Test
  public void stringForm_jaxrs_intf() {
    assertEquals(defaultStr, consumersJaxrs.getIntf().stringForm(null));
  }

  @Test
  public void intForm_jaxrs_rt() {
    assertEquals(defaultInt, (int) consumersJaxrs.getSCBRestTemplate().postForObject("/intForm", null, int.class));
  }

  @Test
  public void doubleForm_jaxrs_rt() {
    assertEquals(defaultDouble,
        consumersJaxrs.getSCBRestTemplate().postForObject("/doubleForm", null, double.class), 0.0);
  }

  @Test
  public void stringForm_jaxrs_rt() {
    assertEquals(defaultStr,
        consumersJaxrs.getSCBRestTemplate().postForObject("/stringForm", null, String.class));
  }

  @Test
  public void intQuery_springmvc_intf() {
    assertEquals(defaultInt, consumersSpringmvc.getIntf().intQuery(null));
  }

  @Test
  public void doubleQuery_springmvc_intf() {
    assertEquals(defaultDouble, consumersSpringmvc.getIntf().doubleQuery(null), 0.0);
  }

  @Test
  public void stringQuery_springmvc_intf() {
    assertEquals(defaultStr, consumersSpringmvc.getIntf().stringQuery(null));
  }

  @Test
  public void intQuery_springmvc_rt() {
    assertEquals(defaultInt, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intQuery", int.class));
  }

  @Test
  public void doubleQuery_springmvc_rt() {
    assertEquals(defaultDouble,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/doubleQuery", double.class), 0.0);
  }

  @Test
  public void stringQuery_springmvc_rt() {
    assertEquals(defaultStr,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/stringQuery", String.class));
  }

  @Test
  public void intHeader_springmvc_intf() {
    assertEquals(defaultInt, consumersSpringmvc.getIntf().intHeader(null));
  }

  @Test
  public void doubleHeader_springmvc_intf() {
    assertEquals(defaultDouble, consumersSpringmvc.getIntf().doubleHeader(null), 0.0);
  }

  @Test
  public void stringHeader_springmvc_intf() {
    assertEquals(defaultStr, consumersSpringmvc.getIntf().stringHeader(null));
  }

  @Test
  public void intHeader_springmvc_rt() {
    assertEquals(defaultInt, (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intHeader", int.class));
  }

  @Test
  public void doubleHeader_springmvc_rt() {
    assertEquals(defaultDouble,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/doubleHeader", double.class), 0.0);
  }

  @Test
  public void stringHeader_springmvc_rt() {
    assertEquals(defaultStr,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/stringHeader", String.class));
  }

  @Test
  public void intForm_springmvc_intf() {
    assertEquals(defaultInt, consumersSpringmvc.getIntf().intForm(null));
  }

  @Test
  public void doubleForm_springmvc_intf() {
    assertEquals(defaultDouble, consumersSpringmvc.getIntf().doubleForm(null), 0.0);
  }

  @Test
  public void stringForm_springmvc_intf() {
    assertEquals(defaultStr, consumersSpringmvc.getIntf().stringForm(null));
  }

  @Test
  public void intForm_springmvc_rt() {
    assertEquals(defaultInt,
        (int) consumersSpringmvc.getSCBRestTemplate().postForObject("/intForm", null, int.class));
  }

  @Test
  public void doubleForm_springmvc_rt() {
    assertEquals(defaultDouble,
        consumersSpringmvc.getSCBRestTemplate().postForObject("/doubleForm", null, double.class), 0.0);
  }

  @Test
  public void stringForm_springmvc_rt() {
    assertEquals(defaultStr,
        consumersSpringmvc.getSCBRestTemplate().postForObject("/stringForm", null, String.class));
  }

  @Test
  public void intQuery_require_springmvc_intf() {
    assertEquals(defaultInt, consumersSpringmvc.getIntf().intQueryRequire(null));
  }

  @Test
  public void doubleQuery_require_springmvc_intf() {
    assertEquals(defaultDouble, consumersSpringmvc.getIntf().doubleQueryRequire(null), 0.0);
  }

  @Test
  public void stringQuery_require_springmvc_intf() {
    assertEquals(defaultStr, consumersSpringmvc.getIntf().stringQueryRequire(null));
  }

  @Test
  public void intQuery_require_springmvc_rt() {
    assertEquals(defaultInt,
        (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intQueryRequire", int.class));
  }

  @Test
  public void doubleQuery_require_springmvc_rt() {
    assertEquals(defaultDouble,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/doubleQueryRequire", double.class), 0.0);
  }

  @Test
  public void stringQuery_require_springmvc_rt() {
    assertEquals(defaultStr,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/stringQueryRequire", String.class));
  }

  @Test
  public void intHeader_require_springmvc_intf() {
    assertEquals(defaultInt, consumersSpringmvc.getIntf().intHeaderRequire(null));
  }

  @Test
  public void doubleHeader_require_springmvc_intf() {
    assertEquals(defaultDouble, consumersSpringmvc.getIntf().doubleHeaderRequire(null), 0.0);
  }

  @Test
  public void stringHeader_require_springmvc_intf() {
    assertEquals(defaultStr, consumersSpringmvc.getIntf().stringHeaderRequire(null));
  }

  @Test
  public void intHeader_require_springmvc_rt() {
    assertEquals(defaultInt,
        (int) consumersSpringmvc.getSCBRestTemplate().getForObject("/intHeaderRequire", int.class));
  }

  @Test
  public void doubleHeader_require_springmvc_rt() {
    assertEquals(defaultDouble,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/doubleHeaderRequire", double.class), 0.0);
  }

  @Test
  public void stringHeader_require_springmvc_rt() {
    assertEquals(defaultStr,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/stringHeaderRequire", String.class));
  }

  @Test
  public void intForm_require_springmvc_intf() {
    assertEquals(defaultInt, consumersSpringmvc.getIntf().intFormRequire(null));
  }

  @Test
  public void doubleForm_require_springmvc_intf() {
    assertEquals(defaultDouble, consumersSpringmvc.getIntf().doubleFormRequire(null), 0.0);
  }

  @Test
  public void stringForm_require_springmvc_intf() {
    assertEquals(defaultStr, consumersSpringmvc.getIntf().stringFormRequire(null));
  }

  @Test
  public void intForm_require_springmvc_rt() {
    assertEquals(defaultInt,
        (int) consumersSpringmvc.getSCBRestTemplate().postForObject("/intFormRequire", null, int.class));
  }

  @Test
  public void doubleForm_require_springmvc_rt() {
    assertEquals(defaultDouble,
        consumersSpringmvc.getSCBRestTemplate().postForObject("/doubleFormRequire", null, double.class), 0.0);
  }

  @Test
  public void stringForm_require_springmvc_rt() {
    assertEquals(defaultStr,
        consumersSpringmvc.getSCBRestTemplate().postForObject("/stringFormRequire", null, String.class));
  }

  //float
  @Test
  public void floatQuery_jaxrs_intf() {
    assertEquals(defaultFloat, consumersJaxrs.getIntf().floatQuery(null), 0.0f);
  }

  @Test
  public void floatQuery_jaxrs_rt() {
    assertEquals(defaultFloat, consumersJaxrs.getSCBRestTemplate().getForObject("/floatQuery", float.class),
        0.0f);
  }

  @Test
  public void floatHeader_jaxrs_intf() {
    assertEquals(defaultFloat, consumersJaxrs.getIntf().floatHeader(null), 0.0f);
  }

  @Test
  public void floatHeader_jaxrs_rt() {
    assertEquals(defaultFloat,
        consumersJaxrs.getSCBRestTemplate().getForObject("/floatHeader", float.class), 0.0f);
  }

  @Test
  public void floatForm_jaxrs_intf() {
    assertEquals(defaultFloat, consumersJaxrs.getIntf().floatForm(null), 0.0f);
  }

  @Test
  public void floatForm_jaxrs_rt() {
    assertEquals(defaultFloat,
        consumersJaxrs.getSCBRestTemplate().postForObject("/floatForm", null, float.class), 0.0f);
  }

  @Test
  public void floatQuery_springmvc_intf() {
    assertEquals(defaultFloat, consumersSpringmvc.getIntf().floatQuery(null), 0.0f);
  }

  @Test
  public void floatQuery_springmvc_rt() {
    assertEquals(defaultFloat,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/floatQuery", float.class), 0.0f);
  }

  @Test
  public void floatHeader_springmvc_intf() {
    assertEquals(defaultFloat, consumersSpringmvc.getIntf().floatHeader(null), 0.0f);
  }

  @Test
  public void floatHeader_springmvc_rt() {
    assertEquals(defaultFloat,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/floatHeader", float.class), 0.0f);
  }

  @Test
  public void floatForm_springmvc_intf() {
    assertEquals(defaultFloat, consumersSpringmvc.getIntf().floatForm(null), 0.0f);
  }

  @Test
  public void floatForm_springmvc_rt() {
    assertEquals(defaultFloat,
        consumersSpringmvc.getSCBRestTemplate().postForObject("/floatForm", null, float.class), 0.0f);
  }

  @Test
  public void floatQuery_require_springmvc_intf() {
    assertEquals(defaultFloat, consumersSpringmvc.getIntf().floatQueryRequire(null), 0.0f);
  }

  @Test
  public void floatQuery_require_springmvc_rt() {
    assertEquals(defaultFloat,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/floatQueryRequire", float.class), 0.0f);
  }

  @Test
  public void floatHeader_require_springmvc_intf() {
    assertEquals(defaultFloat, consumersSpringmvc.getIntf().floatHeaderRequire(null), 0.0f);
  }


  @Test
  public void floatHeader_require_springmvc_rt() {
    assertEquals(defaultFloat,
        consumersSpringmvc.getSCBRestTemplate().getForObject("/floatHeaderRequire", float.class), 0.0f);
  }

  @Test
  public void floatForm_require_springmvc_intf() {
    assertEquals(defaultFloat, consumersSpringmvc.getIntf().floatFormRequire(null), 0.0f);
  }

  @Test
  public void floatForm_require_springmvc_rt() {
    assertEquals(defaultFloat,
        consumersSpringmvc.getSCBRestTemplate().postForObject("/floatFormRequire", null, float.class), 0.0f);
  }
}
