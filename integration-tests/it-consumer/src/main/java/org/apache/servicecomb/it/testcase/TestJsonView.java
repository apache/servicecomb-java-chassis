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
import org.apache.servicecomb.it.schema.PersonViewModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class TestJsonView {
  interface JsonViewRestIntf {
    PersonViewModel jsonViewDefault();

    PersonViewModel jsonViewDefaultWithSummary();

    PersonViewModel jsonViewDefaultWithSummaryDetails();

    PersonViewModel jsonViewProducesDefault();

    PersonViewModel jsonViewProducesDefaultWithSummary();

    PersonViewModel jsonViewProducesDefaultWithSummaryDetails();

    String jsonViewPlainDefault();

    String jsonViewPlainDefaultWithSummary();

    String jsonViewPlainDefaultWithSummaryDetails();

    PersonViewModel jsonViewPostDefault(PersonViewModel personViewModel);

    PersonViewModel jsonViewPostDefaultWithSummary(PersonViewModel personViewModel);

    PersonViewModel jsonViewPostDefaultWithSummaryDetails(PersonViewModel personViewModel);
  }

  private static Consumers<JsonViewRestIntf> consumersPojo = new Consumers<>("jsonViewPojoSchema",
      JsonViewRestIntf.class);

  private static Consumers<JsonViewRestIntf> consumersSpringmvc = new Consumers<>("jsonViewSpringmvcSchema",
      JsonViewRestIntf.class);

  private static Consumers<JsonViewRestIntf> consumersJaxrs = new Consumers<>("jsonViewJaxrsSchema",
      JsonViewRestIntf.class);

  private static final PersonViewModel EXPECT_SUMMARY_VIEW = new PersonViewModel().setName("servicecomb")
      .setEmails("xxx@servicecomb.com");

  private static final PersonViewModel EXPECT_SUMMARY_DETAILS_VIEW = new PersonViewModel().setName("servicecomb")
      .setEmails("xxx@servicecomb.com").setAge(12).setTelephone("xxx10--xx");

  private static final PersonViewModel EXPECT_NO_VIEW = PersonViewModel.generatePersonViewModel();

  @Test
  public void testJsonViewDefault() {
    PersonViewModel restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/jsonViewDefault", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_NO_VIEW, restPersonViewSpringMvc);
    PersonViewModel pojoPersonViewSpringMvc = consumersSpringmvc.getIntf().jsonViewDefault();
    Assertions.assertEquals(EXPECT_NO_VIEW, pojoPersonViewSpringMvc);

    PersonViewModel restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .getForObject("/jsonViewDefault", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_NO_VIEW, restPersonViewJaxrs);
    PersonViewModel pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewDefault();
    Assertions.assertEquals(EXPECT_NO_VIEW, pojoPersonViewJaxrs);

    PersonViewModel restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewDefault", null, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_NO_VIEW, restPersonViewPojo);
    PersonViewModel pojoPersonViewPojo = consumersPojo.getIntf().jsonViewDefault();
    Assertions.assertEquals(EXPECT_NO_VIEW, pojoPersonViewPojo);
  }

  @Test
  public void testJsonViewDefaultWithSummary() {
    PersonViewModel restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/jsonViewDefaultWithSummary", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, restPersonViewSpringMvc);
    PersonViewModel pojoPersonViewSpringMvc = consumersSpringmvc.getIntf().jsonViewDefaultWithSummary();
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, pojoPersonViewSpringMvc);

    PersonViewModel restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .getForObject("/jsonViewDefaultWithSummary", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, restPersonViewJaxrs);
    PersonViewModel pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewDefaultWithSummary();
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, pojoPersonViewJaxrs);

    PersonViewModel restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewDefaultWithSummary", null, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, restPersonViewPojo);
    PersonViewModel pojoPersonViewPojo = consumersPojo.getIntf().jsonViewDefaultWithSummary();
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, pojoPersonViewPojo);
  }

  @Test
  public void testJsonViewDefaultWithSummaryDetails() {
    PersonViewModel restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/jsonViewDefaultWithSummaryDetails", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, restPersonViewSpringMvc);
    PersonViewModel pojoPersonViewSpringMvc = consumersSpringmvc.getIntf().jsonViewDefaultWithSummaryDetails();
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, pojoPersonViewSpringMvc);

    PersonViewModel restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .getForObject("/jsonViewDefaultWithSummaryDetails", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, restPersonViewJaxrs);
    PersonViewModel pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewDefaultWithSummaryDetails();
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, pojoPersonViewJaxrs);

    PersonViewModel restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewDefaultWithSummaryDetails", null, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, restPersonViewPojo);
    PersonViewModel pojoPersonViewPojo = consumersPojo.getIntf().jsonViewDefaultWithSummaryDetails();
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, pojoPersonViewPojo);
  }

  @Test
  public void testJsonViewProducesDefault() {
    PersonViewModel restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/jsonViewProducesDefault", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_NO_VIEW, restPersonViewSpringMvc);
    PersonViewModel pojoPersonViewSpringMvc = consumersSpringmvc.getIntf().jsonViewProducesDefault();
    Assertions.assertEquals(EXPECT_NO_VIEW, pojoPersonViewSpringMvc);

    PersonViewModel restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .getForObject("/jsonViewProducesDefault", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_NO_VIEW, restPersonViewJaxrs);
    PersonViewModel pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewProducesDefault();
    Assertions.assertEquals(EXPECT_NO_VIEW, pojoPersonViewJaxrs);

    PersonViewModel restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewProducesDefault", null, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_NO_VIEW, restPersonViewPojo);
    PersonViewModel pojoPersonViewPojo = consumersPojo.getIntf().jsonViewProducesDefault();
    Assertions.assertEquals(EXPECT_NO_VIEW, pojoPersonViewPojo);
  }

  @Test
  public void testJsonViewProducesDefaultWithSummary() {
    PersonViewModel restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .getForObject("/jsonViewProducesDefaultWithSummary", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, restPersonViewJaxrs);
    PersonViewModel pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewProducesDefaultWithSummary();
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, pojoPersonViewJaxrs);

    PersonViewModel restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/jsonViewProducesDefaultWithSummary", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, restPersonViewSpringMvc);
    PersonViewModel pojoPersonViewSpringMvc = consumersSpringmvc.getIntf().jsonViewProducesDefaultWithSummary();
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, pojoPersonViewSpringMvc);

    PersonViewModel restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewProducesDefaultWithSummary", null, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, restPersonViewPojo);
    PersonViewModel pojoPersonViewPojo = consumersPojo.getIntf().jsonViewProducesDefaultWithSummary();
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, pojoPersonViewPojo);
  }

  @Test
  public void testJsonViewProducesDefaultWithSummaryDetails() {
    PersonViewModel restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/jsonViewProducesDefaultWithSummaryDetails", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, restPersonViewSpringMvc);
    PersonViewModel pojoPersonViewSpringMvc = consumersSpringmvc.getIntf().jsonViewProducesDefaultWithSummaryDetails();
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, pojoPersonViewSpringMvc);

    PersonViewModel restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .getForObject("/jsonViewProducesDefaultWithSummaryDetails", PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, restPersonViewJaxrs);
    PersonViewModel pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewProducesDefaultWithSummaryDetails();
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, pojoPersonViewJaxrs);

    PersonViewModel restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewProducesDefaultWithSummaryDetails", null, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, restPersonViewPojo);
    PersonViewModel pojoPersonViewPojo = consumersPojo.getIntf().jsonViewProducesDefaultWithSummaryDetails();
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, pojoPersonViewPojo);
  }

  @Test
  public void testJsonViewPlainDefault() {
    String restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/jsonViewPlainDefault", String.class);
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), restPersonViewSpringMvc);
    String pojoPersonViewSpringMvc = consumersSpringmvc.getIntf().jsonViewPlainDefault();
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), pojoPersonViewSpringMvc);

    String restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .getForObject("/jsonViewPlainDefault", String.class);
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), restPersonViewJaxrs);
    String pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewPlainDefault();
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), pojoPersonViewJaxrs);

    String restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewPlainDefault", null, String.class);
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), restPersonViewPojo);
    String pojoPersonViewPojo = consumersPojo.getIntf().jsonViewPlainDefault();
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), pojoPersonViewPojo);
  }

  @Test
  public void should_not_affect_text_plain_by_json_view() {
    String restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/jsonViewPlainDefaultWithSummary", String.class);
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), restPersonViewSpringMvc);
    String pojoPersonViewSpringMvc = consumersSpringmvc.getIntf().jsonViewPlainDefaultWithSummary();
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), pojoPersonViewSpringMvc);

    String restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .getForObject("/jsonViewPlainDefaultWithSummary", String.class);
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), restPersonViewJaxrs);
    String pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewPlainDefaultWithSummary();
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), pojoPersonViewJaxrs);

    String restConsumersPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewPlainDefaultWithSummary", null, String.class);
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), restConsumersPojo);
    String pojoConsumersPojo = consumersPojo.getIntf().jsonViewPlainDefaultWithSummary();
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), pojoConsumersPojo);
  }

  @Test
  public void testJsonViewPlainDefaultWithSummaryDetails() {
    String restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .getForObject("/jsonViewPlainDefaultWithSummaryDetails", String.class);
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), restPersonViewSpringMvc);
    String pojoPersonViewSpringMvc = consumersSpringmvc.getIntf().jsonViewPlainDefaultWithSummaryDetails();
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), pojoPersonViewSpringMvc);

    String restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .getForObject("/jsonViewPlainDefaultWithSummaryDetails", String.class);
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), restPersonViewJaxrs);
    String pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewPlainDefaultWithSummaryDetails();
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), pojoPersonViewJaxrs);

    String restConsumersPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewPlainDefaultWithSummaryDetails", null, String.class);
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), restConsumersPojo);
    String pojoConsumersPojo = consumersPojo.getIntf().jsonViewPlainDefaultWithSummaryDetails();
    Assertions.assertEquals(EXPECT_NO_VIEW.toString(), pojoConsumersPojo);
  }

  @Test
  public void testJsonViewPostDefault() {
    PersonViewModel restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/jsonViewPostDefault", EXPECT_NO_VIEW, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_NO_VIEW, restPersonViewSpringMvc);
    PersonViewModel pojoPersonViewSpringMvc = consumersSpringmvc.getIntf()
        .jsonViewPostDefault(EXPECT_NO_VIEW);
    Assertions.assertEquals(EXPECT_NO_VIEW, pojoPersonViewSpringMvc);

    PersonViewModel restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/jsonViewPostDefault", EXPECT_NO_VIEW, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_NO_VIEW, restPersonViewJaxrs);
    PersonViewModel pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewPostDefault(EXPECT_NO_VIEW);
    Assertions.assertEquals(EXPECT_NO_VIEW, pojoPersonViewJaxrs);

    PersonViewModel restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewPostDefault", EXPECT_NO_VIEW, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_NO_VIEW, restPersonViewPojo);
    PersonViewModel pojoPersonViewPojo = consumersPojo.getIntf().jsonViewPostDefault(EXPECT_NO_VIEW);
    Assertions.assertEquals(EXPECT_NO_VIEW, pojoPersonViewPojo);
  }

  @Test
  public void testJsonViewPostDefaultWithSummary() {
    PersonViewModel restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/jsonViewPostDefaultWithSummary", EXPECT_NO_VIEW, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, restPersonViewSpringMvc);
    PersonViewModel pojoPersonViewSpringMvc = consumersSpringmvc.getIntf()
        .jsonViewPostDefaultWithSummary(EXPECT_NO_VIEW);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, pojoPersonViewSpringMvc);

    PersonViewModel restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/jsonViewPostDefaultWithSummary", EXPECT_NO_VIEW, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, restPersonViewJaxrs);
    PersonViewModel pojoPersonViewJaxrs = consumersJaxrs.getIntf().jsonViewPostDefaultWithSummary(EXPECT_NO_VIEW);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, pojoPersonViewJaxrs);

    PersonViewModel restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewPostDefaultWithSummary", EXPECT_NO_VIEW, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, restPersonViewPojo);
    PersonViewModel pojoPersonViewPojo = consumersPojo.getIntf().jsonViewPostDefaultWithSummary(EXPECT_NO_VIEW);
    Assertions.assertEquals(EXPECT_SUMMARY_VIEW, pojoPersonViewPojo);
  }

  @Test
  public void testJsonViewPostDefaultWithSummaryDetails() {
    PersonViewModel restPersonViewSpringMvc = consumersSpringmvc.getSCBRestTemplate()
        .postForObject("/jsonViewPostDefaultWithSummaryDetails", EXPECT_NO_VIEW, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, restPersonViewSpringMvc);
    PersonViewModel pojoPersonViewSpringMvc = consumersSpringmvc.getIntf()
        .jsonViewPostDefaultWithSummaryDetails(EXPECT_NO_VIEW);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, pojoPersonViewSpringMvc);

    PersonViewModel restPersonViewJaxrs = consumersJaxrs.getSCBRestTemplate()
        .postForObject("/jsonViewPostDefaultWithSummaryDetails", EXPECT_NO_VIEW, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, restPersonViewJaxrs);
    PersonViewModel pojoPersonViewJaxrs = consumersJaxrs.getIntf()
        .jsonViewPostDefaultWithSummaryDetails(EXPECT_NO_VIEW);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, pojoPersonViewJaxrs);

    PersonViewModel restPersonViewPojo = consumersPojo.getSCBRestTemplate()
        .postForObject("/jsonViewPostDefaultWithSummaryDetails", EXPECT_NO_VIEW, PersonViewModel.class);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, restPersonViewPojo);
    PersonViewModel pojoPersonViewPojo = consumersPojo.getIntf().jsonViewPostDefaultWithSummaryDetails(EXPECT_NO_VIEW);
    Assertions.assertEquals(EXPECT_SUMMARY_DETAILS_VIEW, pojoPersonViewPojo);
  }
}
