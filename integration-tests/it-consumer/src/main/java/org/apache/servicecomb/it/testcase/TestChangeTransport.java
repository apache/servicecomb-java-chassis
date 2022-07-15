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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class TestChangeTransport {
  interface ChangeTransportIntf {
    String checkTransport();
  }

  static Consumers<ChangeTransportIntf> consumersPojo = new Consumers<>("transportPojo", ChangeTransportIntf.class);

  static Consumers<ChangeTransportIntf> consumersJaxrs = new Consumers<>("transportJaxrs", ChangeTransportIntf.class);

  static Consumers<ChangeTransportIntf> consumersSpringmvc = new Consumers<>("transportSpringmvc",
      ChangeTransportIntf.class);

  void checkTransport_intf(Consumers<ChangeTransportIntf> consumers) {
    Assertions.assertEquals(consumers.getTransport(), consumers.getIntf().checkTransport());
  }

  void checkTransport_rt(Consumers<ChangeTransportIntf> consumers) {
    Assertions.assertEquals(consumers.getTransport(),
        consumers.getSCBRestTemplate().getForObject("/checkTransport", String.class));
  }

  @Test
  public void checkTransport_pojo_intf() {
    checkTransport_intf(consumersPojo);
  }

  @Test
  public void checkTransport_pojo_rt() {
    Assertions.assertEquals(consumersPojo.getTransport(),
        consumersPojo.getSCBRestTemplate().postForObject("/checkTransport", "", String.class));
  }

  @Test
  public void checkTransport_jaxrs_intf() {
    checkTransport_intf(consumersJaxrs);
  }

  @Test
  public void checkTransport_jaxrs_rt() {
    checkTransport_rt(consumersJaxrs);
  }

  @Test
  public void checkTransport_springmvc_intf() {
    checkTransport_intf(consumersSpringmvc);
  }

  @Test
  public void checkTransport_springmvc_rt() {
    checkTransport_rt(consumersSpringmvc);
  }
}
