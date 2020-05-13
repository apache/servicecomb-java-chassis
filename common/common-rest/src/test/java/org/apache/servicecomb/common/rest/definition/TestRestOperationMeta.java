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
import static org.junit.Assert.assertThat;

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
import org.apache.servicecomb.core.SCBEngine;
import org.apache.servicecomb.core.bootstrap.SCBBootstrap;
import org.apache.servicecomb.core.definition.OperationMeta;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.models.Swagger;
import mockit.Expectations;

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

  @BeforeClass
  public static void classSetup() {
    scbEngine = SCBBootstrap.createSCBEngineForTest()
        .addProducerMeta("sid1", new RestOperationMetaSchema())
        .run();
    swagger = scbEngine.getProducerMicroserviceMeta().ensureFindSchemaMeta("sid1").getSwagger();
  }

  @AfterClass
  public static void classTeardown() {
    scbEngine.destroy();
  }

  private void findOperation(String operationId) {
    meta = scbEngine.getProducerMicroserviceMeta().operationMetas().get("test.sid1." + operationId);
    operationMeta = RestMetaUtils.getRestOperationMeta(meta);
  }

  @Test
  public void testCreateProduceProcessorsNull() {
    findOperation("emptyProduces");
    operationMeta.produces = null;
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findProcessor(produce, null);
      Assert.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsNullWithView() {
    findOperation("emptyProducesWithView");
    operationMeta.produces = null;
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findProcessor(produce, Object.class);
      Assert.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsEmpty() {
    findOperation("emptyProduces");
    operationMeta.produces = Arrays.asList();
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findProcessor(produce, null);
      Assert.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsEmptyWithView() {
    findOperation("emptyProducesWithView");
    operationMeta.produces = Arrays.asList();
    operationMeta.createProduceProcessors();

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    for (String produce : ProduceProcessorManager.INSTANCE.keys()) {
      ProduceProcessor expected = ProduceProcessorManager.INSTANCE.findProcessor(produce, Object.class);
      Assert.assertSame(expected, operationMeta.findProduceProcessor(produce));
    }
  }

  @Test
  public void testCreateProduceProcessorsNormal() {
    findOperation("json");

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultJsonProcessor(),
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assert.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsNormalWithView() {
    findOperation("jsonWithView");

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assert.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsNotSupported() {
    findOperation("notSupport");

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessor(),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultJsonProcessor(),
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assert.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsNotSupportedWithView() {
    findOperation("notSupportWithView");

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor((String) null));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor("*/*"));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(ProduceProcessorManager.DEFAULT_TYPE));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultProcessorByViewClass(Object.class),
        operationMeta.findProduceProcessor(MediaType.APPLICATION_JSON));
    Assert.assertNull(operationMeta.findProduceProcessor(MediaType.TEXT_PLAIN));
  }

  @Test
  public void testCreateProduceProcessorsTextAndWildcard() {
    findOperation("textPlain");

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor(),
        operationMeta.ensureFindProduceProcessor(MediaType.WILDCARD));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor(),
        operationMeta.ensureFindProduceProcessor(MediaType.TEXT_PLAIN));
    Assert.assertNull(operationMeta.ensureFindProduceProcessor(MediaType.APPLICATION_JSON));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor(),
        operationMeta.ensureFindProduceProcessor(
            MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML + "," + MediaType.WILDCARD));
  }

  @Test
  public void testCreateProduceProcessorsTextAndWildcardWithView() {
    findOperation("textPlainWithView");

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findPlainProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(MediaType.WILDCARD));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findPlainProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(MediaType.TEXT_PLAIN));
    Assert.assertNull(operationMeta.ensureFindProduceProcessor(MediaType.APPLICATION_JSON));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findPlainProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(
            MediaType.APPLICATION_JSON + "," + MediaType.APPLICATION_XML + "," + MediaType.WILDCARD));
  }

  @Test
  public void testCreateProduceProcessorsWithSemicolon() {
    findOperation("textCharJsonChar");

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultPlainProcessor(),
        operationMeta.ensureFindProduceProcessor(MediaType.TEXT_PLAIN));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findDefaultJsonProcessor(),
        operationMeta.ensureFindProduceProcessor(MediaType.APPLICATION_JSON));
  }

  @Test
  public void testCreateProduceProcessorsWithSemicolonWithView() {
    findOperation("textCharJsonCharWithView");

    Assert.assertSame(ProduceProcessorManager.INSTANCE.findPlainProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(MediaType.TEXT_PLAIN));
    Assert.assertSame(ProduceProcessorManager.INSTANCE.findJsonProcessorByViewClass(Object.class),
        operationMeta.ensureFindProduceProcessor(MediaType.APPLICATION_JSON));
  }

  @Test
  public void testEnsureFindProduceProcessorAcceptNotFound() {
    findOperation("textCharJsonChar");
    Assert.assertNull(operationMeta.ensureFindProduceProcessor("notSupport"));
  }

  @Test
  public void testEnsureFindProduceProcessorAcceptNotFoundWithView() {
    findOperation("textCharJsonCharWithView");
    Assert.assertNull(operationMeta.ensureFindProduceProcessor("notSupport"));
  }

  @Test
  public void generatesAbsolutePathWithRootBasePath() {
    findOperation("textCharJsonChar");

    assertThat(operationMeta.getAbsolutePath(), is("/textCharJsonChar/"));
  }

  @Test
  public void generatesAbsolutePathWithNonRootBasePath() {
    findOperation("textCharJsonChar");
    new Expectations(swagger) {
      {
        swagger.getBasePath();
        result = "/rest";
      }
    };
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(operationMeta.getOperationMeta());

    assertThat(restOperationMeta.getAbsolutePath(), is("/rest/textCharJsonChar/"));
  }

  @Test
  public void generatesAbsolutePathWithNullPath() {
    findOperation("textCharJsonChar");
    new Expectations(swagger) {
      {
        swagger.getBasePath();
        result = null;
      }
    };
    new Expectations(meta) {
      {
        meta.getOperationPath();
        result = null;
      }
    };
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(operationMeta.getOperationMeta());

    assertThat(restOperationMeta.getAbsolutePath(), is("/"));
  }

  @Test
  public void generatesAbsolutePathWithEmptyPath() {
    findOperation("textCharJsonChar");
    new Expectations(swagger) {
      {
        swagger.getBasePath();
        result = "";
      }
    };
    new Expectations(meta) {
      {
        meta.getOperationPath();
        result = "";
      }
    };
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(operationMeta.getOperationMeta());

    assertThat(restOperationMeta.getAbsolutePath(), is("/"));
  }

  @Test
  public void consecutiveSlashesAreRemoved() {
    findOperation("textCharJsonChar");
    new Expectations(swagger) {
      {
        swagger.getBasePath();
        result = "//rest//";
      }
    };
    new Expectations(meta) {
      {
        meta.getOperationPath();
        result = "//sayHi//";
      }
    };
    RestOperationMeta restOperationMeta = new RestOperationMeta();
    restOperationMeta.init(operationMeta.getOperationMeta());

    assertThat(restOperationMeta.getAbsolutePath(), is("/rest/sayHi/"));
  }

  @Test
  public void testFormDataFlagTrue() {
    findOperation("form");

    assertThat(operationMeta.isFormData(), is(true));
  }

  @Test
  public void testFormDataFlagFalse() {
    findOperation("json");

    assertThat(operationMeta.isFormData(), is(false));
  }
}
