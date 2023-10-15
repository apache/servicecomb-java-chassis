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

package org.apache.servicecomb.common.rest.definition;

import static org.apache.servicecomb.core.SCBEngine.CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC;
import static org.apache.servicecomb.core.SCBEngine.DEFAULT_TURN_DOWN_STATUS_WAIT_SEC;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.common.rest.RestEngineSchemaListener;
import org.apache.servicecomb.core.BootListener;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.core.executor.ExecutorManager;
import org.apache.servicecomb.core.transport.TransportManager;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class TestRestOperationMeta {
  @Path("/")
  static class RestOperationMetaSchema {
    @Path("/emptyProduces")
    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public String emptyProduces() {
      return null;
    }

    @Path("/emptyProducesWithView")
    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @JsonView(Object.class)
    public String emptyProducesWithView() {
      return null;
    }

    @Path("/notSupport")
    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    public void notSupport() {

    }

    @Path("/notSupportWithView")
    @GET
    @Produces(value = MediaType.APPLICATION_JSON)
    @JsonView(Object.class)
    public void notSupportWithView() {

    }

    @Path("/textPlain")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String textPlain() {
      return null;
    }

    @Path("/textPlainWithView")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @JsonView(Object.class)
    public String textPlainWithView() {
      return null;
    }

    @Path("/json")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String json() {
      return null;
    }

    @Path("/jsonWithView")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView(Object.class)
    public String jsonWithView() {
      return null;
    }

    @Path("/textCharJsonChar")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public void textCharJsonChar() {

    }

    @Path("/textCharJsonCharWithView")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @JsonView(Object.class)
    public void textCharJsonCharWithView() {

    }

    @Path("/download")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public File download() {
      return null;
    }

    @Path("/downloadWithView")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView(Object.class)
    public File downloadWithView() {
      return null;
    }

    @Path("/form")
    @POST
    public void form(@FormParam("form") String form) {
    }

    @Path("/formWithView")
    @POST
    public void formWithView(@FormParam("form") String form) {
    }
  }

  static SCBEngine scbEngine;

  static OpenAPI swagger;

  OperationMeta meta;

  RestOperationMeta operationMeta;

  @BeforeAll
  public static void classSetup() {
    Environment environment = Mockito.mock(Environment.class);
    scbEngine = SCBBootstrap.createSCBEngineForTest(environment);
    ExecutorManager executorManager = Mockito.mock(ExecutorManager.class);
    TransportManager transportManager = Mockito.mock(TransportManager.class);
    scbEngine.setTransportManager(transportManager);
    scbEngine.setExecutorManager(executorManager);
    scbEngine.setEnvironment(environment);
    Mockito.when(environment.getProperty(CFG_KEY_TURN_DOWN_STATUS_WAIT_SEC,
        long.class, DEFAULT_TURN_DOWN_STATUS_WAIT_SEC)).thenReturn(DEFAULT_TURN_DOWN_STATUS_WAIT_SEC);

    List<BootListener> listeners = new ArrayList<>();
    listeners.add(new RestEngineSchemaListener());
    scbEngine.setBootListeners(listeners);
    scbEngine
        .addProducerMeta("sid1", new RestOperationMetaSchema())
        .run();
    swagger = Mockito.spy(scbEngine.getProducerMicroserviceMeta().ensureFindSchemaMeta("sid1").getSwagger());
  }

  @AfterAll
  public static void classTeardown() {
    scbEngine.destroy();
  }

  private void findOperation(String operationId) {
    meta = Mockito.spy(scbEngine.getProducerMicroserviceMeta().operationMetas().get("test.sid1." + operationId));
    operationMeta = Mockito.spy(RestMetaUtils.getRestOperationMeta(meta));
    SchemaMeta schemaMeta = Mockito.spy(meta.getSchemaMeta());
    Mockito.when(meta.getSchemaMeta()).thenReturn(schemaMeta);
    Mockito.when(schemaMeta.getSwagger()).thenReturn(swagger);
  }

  @Test
  public void generatesAbsolutePathWithRootBasePath() {
    findOperation("textCharJsonChar");

    MatcherAssert.assertThat(operationMeta.getAbsolutePath(), is("/textCharJsonChar/"));
  }

  @Test
  public void generatesAbsolutePathWithNonRootBasePath() {
    findOperation("textCharJsonChar");
    Server server = Mockito.mock(Server.class);
    Mockito.when(server.getUrl()).thenReturn("/rest");
    Mockito.when(swagger.getServers()).thenReturn(Arrays.asList(server));
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(meta);

    MatcherAssert.assertThat(restOperationMeta.getAbsolutePath(), is("/rest/textCharJsonChar/"));
  }

  @Test
  public void generatesAbsolutePathWithNullPath() {
    findOperation("textCharJsonChar");
    Server server = Mockito.mock(Server.class);
    Mockito.when(server.getUrl()).thenReturn(null);
    Mockito.when(swagger.getServers()).thenReturn(Arrays.asList(server));
    Mockito.when(meta.getOperationPath()).thenReturn(null);
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(meta);

    MatcherAssert.assertThat(restOperationMeta.getAbsolutePath(), is("/"));
  }

  @Test
  public void generatesAbsolutePathWithEmptyPath() {
    findOperation("textCharJsonChar");
    Server server = Mockito.mock(Server.class);
    Mockito.when(server.getUrl()).thenReturn("");
    Mockito.when(swagger.getServers()).thenReturn(Arrays.asList(server));
    Mockito.when(meta.getOperationPath()).thenReturn("");
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(meta);

    MatcherAssert.assertThat(restOperationMeta.getAbsolutePath(), is("/"));
  }

  @Test
  public void consecutiveSlashesAreRemoved() {
    findOperation("textCharJsonChar");
    Server server = Mockito.mock(Server.class);
    Mockito.when(server.getUrl()).thenReturn("//rest//");
    Mockito.when(swagger.getServers()).thenReturn(Arrays.asList(server));
    Mockito.when(meta.getOperationPath()).thenReturn("//sayHi//");
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(meta);

    MatcherAssert.assertThat(restOperationMeta.getAbsolutePath(), is("/rest/sayHi/"));
  }

  @Test
  public void testFormDataFlagTrue() {
    findOperation("form");

    MatcherAssert.assertThat(operationMeta.isFormData(), is(true));
  }

  @Test
  public void testFormDataFlagFalse() {
    findOperation("json");

    MatcherAssert.assertThat(operationMeta.isFormData(), is(false));
  }
}
