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
package org.apache.servicecomb.codec.protobuf.internal.converter;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.codec.protobuf.internal.converter.model.ProtoSchema;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGenerator;
import org.junit.Assert;
import org.junit.Test;

import io.protostuff.compiler.model.Proto;
import io.swagger.models.Swagger;

public class TestSwaggerToProtoGenerator {
  @Test
  public void convert() throws IOException {
    URL url = TestSwaggerToProtoGenerator.class.getClassLoader().getResource("ProtoSchema.proto");
    String protoContent = IOUtils.toString(url, "UTF-8");
    int idx = protoContent.indexOf("syntax = ");
    protoContent = protoContent.substring(idx);

    SpringmvcSwaggerGenerator swaggerGenerator = new SpringmvcSwaggerGenerator(ProtoSchema.class);
    Swagger swagger = swaggerGenerator.generate();

    SwaggerToProtoGenerator generator = new SwaggerToProtoGenerator("a.b", swagger);
    Proto proto = generator.convert();

    Assert.assertEquals(protoContent.replaceAll("\r\n", "\n"),
        new ProtoToStringGenerator(proto).protoToString().replaceAll("\r\n", "\n"));
  }

  @Test
  public void testEscape() {
    Assert.assertEquals("hello_my_service", SwaggerToProtoGenerator.escapeMessageName("hello.my.service"));
    Assert.assertEquals("hello_my_service", SwaggerToProtoGenerator.escapeMessageName("hello_my_service"));
    Assert.assertEquals("hello.my_service", SwaggerToProtoGenerator.escapePackageName("hello.my-service"));
    Assert.assertEquals("hello.test.test", SwaggerToProtoGenerator.escapePackageName("hello.test.test"));
    Assert.assertEquals("hello_my.test.test", SwaggerToProtoGenerator.escapePackageName("hello:my.test.test"));
    Assert.assertEquals(false, SwaggerToProtoGenerator.isValidEnum("hello.test.test"));
    Assert.assertEquals(false, SwaggerToProtoGenerator.isValidEnum("hello.my-service"));
    Assert.assertEquals(true, SwaggerToProtoGenerator.isValidEnum("My_ENum"));
  }
}
