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
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.servicecomb.codec.protobuf.internal.converter.model.ProtoSchema;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.SwaggerGeneratorContext;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGeneratorContext;
import org.junit.Assert;
import org.junit.Test;

import io.protostuff.compiler.model.Proto;
import io.swagger.models.Swagger;

public class TestSwaggerToProtoGenerator {
  @Test
  public void convert() throws IOException {
    URL url = TestSwaggerToProtoGenerator.class.getClassLoader().getResource("ProtoSchema.proto");
    String protoContent = IOUtils.toString(url, StandardCharsets.UTF_8);
    int idx = protoContent.indexOf("syntax = ");
    protoContent = protoContent.substring(idx);

    SwaggerGeneratorContext context = new SpringmvcSwaggerGeneratorContext();
    SwaggerGenerator swaggerGenerator = new SwaggerGenerator(context, ProtoSchema.class);
    Swagger swagger = swaggerGenerator.generate();

    SwaggerToProtoGenerator generator = new SwaggerToProtoGenerator("a.b", swagger);
    Proto proto = generator.convert();

    Assert.assertEquals(protoContent.replaceAll("\r\n", "\n"),
        new ProtoToStringGenerator(proto).protoToString().replaceAll("\r\n", "\n"));
  }
}
