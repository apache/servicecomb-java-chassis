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

package org.apache.servicecomb.swagger.generator.core.processor.annotation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.servicecomb.swagger.generator.core.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.core.SwaggerGenerator;
import org.springframework.util.StringUtils;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.util.BaseReaderUtils;

public class SwaggerDefinitionProcessor implements ClassAnnotationProcessor {
  @Override
  public void process(Object annotation, SwaggerGenerator swaggerGenerator) {
    SwaggerDefinition definitionAnnotation = (SwaggerDefinition) annotation;
    Swagger swagger = swaggerGenerator.getSwagger();

    swaggerGenerator.setBasePath(definitionAnnotation.basePath());
    swagger.setHost(definitionAnnotation.host());

    convertConsumes(definitionAnnotation, swagger);
    convertProduces(definitionAnnotation, swagger);
    convertSchemes(definitionAnnotation, swagger);
    convertTags(definitionAnnotation, swagger);
    convertInfo(definitionAnnotation.info(), swagger);
    swagger.setExternalDocs(convertExternalDocs(definitionAnnotation.externalDocs()));
  }

  private void convertInfo(io.swagger.annotations.Info infoAnnotation, Swagger swagger) {
    if (infoAnnotation == null) {
      return;
    }

    Info info = new Info();

    info.setTitle(infoAnnotation.title());
    info.setVersion(infoAnnotation.version());
    info.setDescription(infoAnnotation.description());
    info.setTermsOfService(infoAnnotation.termsOfService());
    info.setContact(convertContact(infoAnnotation.contact()));
    info.setLicense(convertLicense(infoAnnotation.license()));
    info.getVendorExtensions().putAll(BaseReaderUtils.parseExtensions(infoAnnotation.extensions()));

    swagger.setInfo(info);
  }

  private License convertLicense(io.swagger.annotations.License licenseAnnotation) {
    License license = new License();

    license.setName(licenseAnnotation.name());
    license.setUrl(licenseAnnotation.url());

    return license;
  }

  private Contact convertContact(io.swagger.annotations.Contact contactAnnotation) {
    Contact contact = new Contact();

    contact.setName(contactAnnotation.name());
    contact.setUrl(contactAnnotation.url());
    contact.setEmail(contactAnnotation.email());

    return contact;
  }

  private void convertTags(SwaggerDefinition definitionAnnotation, Swagger swagger) {
    if (definitionAnnotation.tags() == null) {
      return;
    }

    Stream<io.swagger.annotations.Tag> stream =
        Arrays.asList(definitionAnnotation.tags()).stream();
    List<Tag> tags = stream.map(this::convertTag).collect(Collectors.toList());
    swagger.setTags(tags);
  }

  private Tag convertTag(io.swagger.annotations.Tag tagAnnotation) {
    Tag tag = new Tag();
    tag.setName(tagAnnotation.name());
    tag.setDescription(tagAnnotation.description());
    tag.setExternalDocs(convertExternalDocs(tagAnnotation.externalDocs()));
    tag.getVendorExtensions().putAll(BaseReaderUtils.parseExtensions(tagAnnotation.extensions()));
    return tag;
  }

  private ExternalDocs convertExternalDocs(io.swagger.annotations.ExternalDocs annotationExternalDocs) {
    ExternalDocs externalDocs = new ExternalDocs();
    externalDocs.setUrl(annotationExternalDocs.url());
    externalDocs.setDescription(annotationExternalDocs.value());

    return externalDocs;
  }

  private void convertSchemes(SwaggerDefinition definitionAnnotation, Swagger swagger) {
    if (definitionAnnotation.schemes() == null) {
      return;
    }

    Stream<io.swagger.annotations.SwaggerDefinition.Scheme> stream =
        Arrays.asList(definitionAnnotation.schemes()).stream();
    List<Scheme> schemes = stream.map(this::convertScheme).collect(Collectors.toList());
    swagger.setSchemes(schemes);
  }

  private Scheme convertScheme(io.swagger.annotations.SwaggerDefinition.Scheme annotationScheme) {
    return Scheme.forValue(annotationScheme.name());
  }

  private void convertProduces(SwaggerDefinition definitionAnnotation, Swagger swagger) {
    String[] produces = definitionAnnotation.produces();
    if (produces == null) {
      return;
    }
    List<String> produceList = Arrays.stream(produces).filter(s -> !StringUtils.isEmpty(s))
        .collect(Collectors.toList());
    if (!produceList.isEmpty()) {
      swagger.setProduces(produceList);
    }
  }

  private void convertConsumes(SwaggerDefinition definitionAnnotation, Swagger swagger) {
    String[] consumes = definitionAnnotation.consumes();
    if (consumes == null) {
      return;
    }
    List<String> consumeList = Arrays.stream(consumes).filter(s -> !StringUtils.isEmpty(s))
        .collect(Collectors.toList());
    if (!consumeList.isEmpty()) {
      swagger.setConsumes(consumeList);
    }
  }
}
