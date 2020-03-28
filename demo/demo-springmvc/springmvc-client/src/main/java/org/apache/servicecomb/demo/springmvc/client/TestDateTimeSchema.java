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

package org.apache.servicecomb.demo.springmvc.client;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.core.Endpoint;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.Transport;
import org.apache.servicecomb.demo.CategorizedTestCase;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.foundation.common.cache.VersionedCache;
import org.apache.servicecomb.loadbalance.LoadbalanceHandler;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.springmvc.reference.CseHttpEntity;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryContext;
import org.apache.servicecomb.serviceregistry.discovery.DiscoveryTree;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

interface DateTimeSchemaInf {
  Date getDate(Date date);

  Date getDatePath(Date date);

  Date postDate(Date date);

  LocalDate getLocalDate(LocalDate date);

  LocalDate getLocalDatePath(LocalDate date);

  LocalDate postLocalDate(LocalDate date);

  LocalDateTime getLocalDateTime(LocalDateTime date);

  LocalDateTime getLocalDateTimePath(LocalDateTime date);

  LocalDateTime postLocalDateTime(LocalDateTime date);
}

interface DateTimeSchemaWithContextInf {
  Date getDate(InvocationContext context, Date date);
}

@Component
public class TestDateTimeSchema implements CategorizedTestCase {
  @RpcReference(microserviceName = "springmvc", schemaId = "DateTimeSchema")
  private DateTimeSchemaInf dateTimeSchemaInf;

  @RpcReference(microserviceName = "springmvc", schemaId = "DateTimeSchema")
  private DateTimeSchemaWithContextInf dateTimeSchemaWithContextInf;

  private DiscoveryTree discoveryTree = new DiscoveryTree();

  public TestDateTimeSchema() {
    discoveryTree.addFilter(new CustomEndpointDiscoveryFilter());
    discoveryTree.sort();
  }

  @Override
  public void testRestTransport() throws Exception {

  }

  @Override
  public void testHighwayTransport() throws Exception {

  }

  @Override
  public void testAllTransport() throws Exception {
    testDateTimeSchema();
    testDateTimeSchemaMulticast();
    testDateTimeSchemaMulticastRestTemplate();
  }

  private void testDateTimeSchema() {
    Date date = new Date();
    TestMgr.check(date.getTime(), dateTimeSchemaInf.getDate(date).getTime());
    TestMgr.check(date.getTime(), dateTimeSchemaInf.getDatePath(date).getTime());
    TestMgr.check(date.getTime(), dateTimeSchemaInf.postDate(date).getTime());

    LocalDate localDate = LocalDate.of(2020, 2, 1);
    TestMgr.check(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        dateTimeSchemaInf.getLocalDate(localDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    TestMgr.check(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        dateTimeSchemaInf.getLocalDatePath(localDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    TestMgr.check(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        dateTimeSchemaInf.postLocalDate(localDate).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

    LocalDateTime localDateTime = LocalDateTime.of(2020, 2, 1, 23, 23, 30, 333);
    TestMgr.check(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")),
        dateTimeSchemaInf.getLocalDateTime(localDateTime)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
    TestMgr.check(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")),
        dateTimeSchemaInf.getLocalDateTimePath(localDateTime)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
    TestMgr.check(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")),
        dateTimeSchemaInf.postLocalDateTime(localDateTime)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
  }

  private void testDateTimeSchemaMulticast() throws Exception {
    DiscoveryContext context = new DiscoveryContext();
    VersionedCache serversVersionedCache = discoveryTree.discovery(context, "springmvctest", "springmvc", "0+");
    List<String> enpoints = serversVersionedCache.data();

    for (String endpoint : enpoints) {
      InvocationContext invocationContext = new InvocationContext();
      invocationContext.addLocalContext(LoadbalanceHandler.SERVICECOMB_SERVER_ENDPOINT, endpoint);
      Date date = new Date();
      TestMgr.check(date.getTime(), dateTimeSchemaWithContextInf.getDate(invocationContext, date).getTime());

      invocationContext = new InvocationContext();
      invocationContext.addLocalContext(LoadbalanceHandler.SERVICECOMB_SERVER_ENDPOINT, parseEndpoint(endpoint));
      date = new Date();
      TestMgr.check(date.getTime(), dateTimeSchemaWithContextInf.getDate(invocationContext, date).getTime());
    }
  }

  private Endpoint parseEndpoint(String endpointUri) throws Exception {
    URI formatUri = new URI(endpointUri);
    Transport transport = SCBEngine.getInstance().getTransportManager().findTransport(formatUri.getScheme());
    return new Endpoint(transport, endpointUri);
  }

  private void testDateTimeSchemaMulticastRestTemplate() throws Exception {
    DiscoveryContext context = new DiscoveryContext();
    VersionedCache serversVersionedCache = discoveryTree.discovery(context, "springmvctest", "springmvc", "0+");
    List<String> enpoints = serversVersionedCache.data();

    RestTemplate restTemplate = RestTemplateBuilder.create();

    for (String endpoint : enpoints) {
      CseHttpEntity<?> entity = new CseHttpEntity<>(null);
      InvocationContext invocationContext = new InvocationContext();
      invocationContext.addLocalContext(LoadbalanceHandler.SERVICECOMB_SERVER_ENDPOINT, endpoint);
      entity.setContext(invocationContext);

      Date date = new Date();
      String dateValue = RestObjectMapperFactory.getRestObjectMapper().convertToString(date);
      TestMgr.check(date.getTime(),
          restTemplate
              .exchange("cse://springmvc/dateTime/getDate?date={1}", HttpMethod.GET,
                  entity, Date.class, dateValue).getBody().getTime());

      entity = new CseHttpEntity<>(null);
      invocationContext = new InvocationContext();
      invocationContext.addLocalContext(LoadbalanceHandler.SERVICECOMB_SERVER_ENDPOINT, parseEndpoint(endpoint));
      entity.setContext(invocationContext);

      date = new Date();
      dateValue = RestObjectMapperFactory.getRestObjectMapper().convertToString(date);
      TestMgr.check(date.getTime(),
          restTemplate
              .exchange("cse://springmvc/dateTime/getDate?date={1}", HttpMethod.GET,
                  entity, Date.class, dateValue).getBody().getTime());
    }
  }
}
