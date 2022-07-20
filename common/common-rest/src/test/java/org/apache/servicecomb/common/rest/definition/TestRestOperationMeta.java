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

import static org.hamcrest.core.Is.is;

import java.io.File;
import java.util.Arrays;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessor;
import org.apache.servicecomb.common.rest.codec.produce.ProduceProcessorManager;
import org.apache.servicecomb.config.ConfigUtil;
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.apache.servicecomb.core.definition.SchemaMeta;
import org.apache.servicecomb.foundation.test.scaffolding.config.ArchaiusUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.models.Swagger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TestRestOperationMeta {
  @Path("/")
  static class RestOperationMetaSchema {
    @Path("/emptyProduces")
    @GET
    @Produces("")
    public String emptyProduces() {
      return null;
    }

    @Path("/emptyProducesWithView")
    @GET
    @Produces("")
    @JsonView(Object.class)
    public String emptyProducesWithView() {
      return null;
    }

    @Path("/notSupport")
    @GET
    @Produces("notSupport")
    public void notSupport() {

    }

    @Path("/notSupportWithView")
    @GET
    @Produces("notSupport")
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
    @Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8", MediaType.TEXT_PLAIN + ";charset=UTF-8"})
    public void textCharJsonChar() {

    }

    @Path("/textCharJsonCharWithView")
    @GET
    @Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8", MediaType.TEXT_PLAIN + ";charset=UTF-8"})
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

  static Swagger swagger;

  OperationMeta meta;

  RestOperationMeta operationMeta;

  @BeforeAll
  public static void classSetup() {
    ConfigUtil.installDynamicConfig();
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new RestOperationMetaSchema())
        .run();
    swagger = Mockito.spy(scbEngine.getProducerMicroserviceMeta().ensureFindSchemaMeta("sid1").getSwagger());
  }

  @AfterAll
  public static void classTeardown() {
    scbEngine.destroy();
    ArchaiusUtils.resetConfig();
  }

  private void findOperation(String operationId) {
    meta = Mockito.spy(scbEngine.getProducerMicroserviceMeta().operationMetas().get("test.sid1." + operationId));
    operationMeta = Mockito.spy(RestMetaUtils.getRestOperationMeta(meta));
    SchemaMeta schemaMeta = Mockito.spy(meta.getSchemaMeta());
    Mockito.when(meta.getSchemaMeta()).thenReturn(schemaMeta);
    Mockito.when(schemaMeta.getSwagger()).thenReturn(swagger);
  }

  @Test
  public void testCreateProduceProcessorsNull() {
    findOperation("emptyProduces");
    operationMeta.produces = null;
    operationMeta.createProduceProcessors();

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findProcessor(produce, null);
      Assertions.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsNullWithView() {
    findOperation("emptyProducesWithView");
    operationMeta.produces = null;
    operationMeta.createProduceProcessors();

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findProcessor(produce, Object.class);
      Assertions.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsEmpty() {
    findOperation("emptyProduces");
    operationMeta.produces = Arrays.asList();
    operationMeta.createProduceProcessors();

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findProcessor(produce, null);
      Assertions.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsEmptyWithView() {
    findOperation("emptyProducesWithView");
    operationMeta.produces = Arrays.asList();
    operationMeta.createProduceProcessors();

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findProcessor(produce, Object.class);
      Assertions.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsNormal() {
    findOperation("json");

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultJsonProcessor(),
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assertions.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsNormalWithView() {
    findOperation("jsonWithView");

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assertions.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsNotSupported() {
    findOperation("notSupport");

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultJsonProcessor(),
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assertions.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsNotSupportedWithView() {
    findOperation("notSupportWithView");

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assertions.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsTextAndWildcard() {
    findOperation("textPlain");

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor(),
        operationMeta.ensureFindProduceProcessor(MediaType.WILDCARD));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor(),
        operationMeta.ensureFindProduceProcessor(MediaType.TEXT_PLAIN));
    Assertions.assertNull(operationMeta.ensureFindProduceProcessor(MediaType.APPLICATION_JSON));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor(),
        operationMeta.ensureFindProduceProcessor(
            MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML + "," + MediaType.WILDCARD));
  }

  @Test
  public void testCreateProduceProcessorsTextAndWildcardWithView() {
    findOperation("textPlainWithView");

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findPlainProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(MediaType.WILDCARD));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findPlainProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(MediaType.TEXT_PLAIN));
    Assertions.assertNull(operationMeta.ensureFindProduceProcessor(MediaType.APPLICATION_JSON));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findPlainProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(
            MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML + "," + MediaType.WILDCARD));
  }

  @Test
  public void testCreateProduceProcessorsWithSemicolon() {
    findOperation("textCharJsonChar");

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor(),
        operationMeta.ensureFindProduceProcessor(MediaType.TEXT_PLAIN));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findDefaultJsonProcessor(),
        operationMeta.ensureFindProduceProcessor(MediaType.APPLICATION_JSON));
  }

  @Test
  public void testCreateProduceProcessorsWithSemicolonWithView() {
    findOperation("textCharJsonCharWithView");

    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findPlainProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(MediaType.TEXT_PLAIN));
    Assertions.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(MediaType.APPLICATION_JSON));
  }

  @Test
  public void testEnsureFindProduceProcessorAcceptNotFound() {
    findOperation("textCharJsonChar");
    Assertions.assertNull(operationMeta.ensureFindProduceProcessor("notSupport"));
  }

  @Test
  public void testEnsureFindProduceProcessorAcceptNotFoundWithView() {
    findOperation("textCharJsonCharWithView");
    Assertions.assertNull(operationMeta.ensureFindProduceProcessor("notSupport"));
  }

  @Test
  public void generatesAbsolutePathWithRootBasePath() {
    findOperation("textCharJsonChar");

    MatcherAssert.assertThat(operationMeta.getAbsolutePath(), is("/textCharJsonChar/"));
  }

  @Test
  public void generatesAbsolutePathWithNonRootBasePath() {
    findOperation("textCharJsonChar");
    Mockito.when(swagger.getBasePath()).thenReturn("/rest");
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(meta);

    MatcherAssert.assertThat(restOperationMeta.getAbsolutePath(), is("/rest/textCharJsonChar/"));
  }

  @Test
  public void generatesAbsolutePathWithNullPath() {
    findOperation("textCharJsonChar");
    Mockito.when(swagger.getBasePath()).thenReturn(null);
    Mockito.when(meta.getOperationPath()).thenReturn(null);
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(meta);

    MatcherAssert.assertThat(restOperationMeta.getAbsolutePath(), is("/"));
  }

  @Test
  public void generatesAbsolutePathWithEmptyPath() {
    findOperation("textCharJsonChar");
    Mockito.when(swagger.getBasePath()).thenReturn("");
    Mockito.when(meta.getOperationPath()).thenReturn("");
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(meta);

    MatcherAssert.assertThat(restOperationMeta.getAbsolutePath(), is("/"));
  }

  @Test
  public void consecutiveSlashesAreRemoved() {
    findOperation("textCharJsonChar");
    Mockito.when(swagger.getBasePath()).thenReturn("//rest//");
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
