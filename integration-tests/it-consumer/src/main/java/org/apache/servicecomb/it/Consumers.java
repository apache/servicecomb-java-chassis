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
package org.apache.servicecomb.it;

import org.apache.servicecomb.it.extend.engine.GateRestTemplate;
import org.apache.servicecomb.it.extend.engine.ITInvoker;
import org.apache.servicecomb.it.extend.engine.ITSCBRestTemplate;
import org.apache.servicecomb.it.junit.ITJUnitUtils;
import org.springframework.web.client.RestTemplate;

public class Consumers<INTF> {
  private String producerName;

  private String schemaId;

  private Class<INTF> intfCls;

  private ITSCBRestTemplate scbRestTemplate;

  private RestTemplate edgeRestTemplate;

  private RestTemplate zuulRestTemplate;

  private INTF intf;

  private String transport;

  public Consumers(String schemaId, Class<INTF> intfCls) {
    this.schemaId = schemaId;
    this.intfCls = intfCls;
  }

  public void init() {
    this.producerName = ITJUnitUtils.getProducerName();
    this.transport = ITJUnitUtils.getTransport();
    intf = ITInvoker.createProxy(producerName, schemaId, transport, intfCls);
    scbRestTemplate = new ITSCBRestTemplate(schemaId).init();
    edgeRestTemplate = GateRestTemplate.createEdgeRestTemplate(schemaId).init();
    zuulRestTemplate = null;// GateRestTemplate.createZuulRestTemplate(schemaId).init();
  }

  public String getSchemaId() {
    return schemaId;
  }

  public INTF getIntf() {
    return intf;
  }

  public RestTemplate getSCBRestTemplate() {
    return scbRestTemplate;
  }

  public RestTemplate getEdgeRestTemplate() {
    return edgeRestTemplate;
  }

  public RestTemplate getZuulRestTemplate() {
    return zuulRestTemplate;
  }

  public String getTransport() {
    return transport;
  }
}