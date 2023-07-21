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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.generator.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

public class SwaggerDefinitionProcessor implements ClassAnnotationProcessor<OpenAPIDefinition> {
  @Override
  public Type getProcessType() {
    return OpenAPIDefinition.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, OpenAPIDefinition definitionAnnotation) {
    OpenAPI swagger = swaggerGenerator.getOpenAPI();

    if (definitionAnnotation.servers() != null) {
      swagger.setServers(convertServers(definitionAnnotation.servers()));
    }

    swagger.setTags(convertTags(definitionAnnotation.tags()));
    swagger.setInfo(convertInfo(definitionAnnotation.info()));
  }

  private List<Server> convertServers(io.swagger.v3.oas.annotations.servers.Server[] servers) {
    if (servers == null) {
      return null;
    }

    return Arrays.stream(servers).map(this::convertServer).collect(Collectors.toList());
  }

  private Server convertServer(io.swagger.v3.oas.annotations.servers.Server server) {
    Server item = new Server();
    item.setUrl(server.url());
    item.setDescription(server.description());
    // TODO: add other information
    return item;
  }

  private Info convertInfo(io.swagger.v3.oas.annotations.info.Info infoAnnotation) {
    if (infoAnnotation == null) {
      return null;
    }

    Info info = new Info();

    info.setTitle(infoAnnotation.title());
    info.setVersion(infoAnnotation.version());
    if (StringUtils.isNotEmpty(infoAnnotation.description())) {
      info.setDescription(infoAnnotation.description());
    }
    if (StringUtils.isNotEmpty(infoAnnotation.termsOfService())) {
      info.setTermsOfService(infoAnnotation.termsOfService());
    }
    info.setContact(convertContact(infoAnnotation.contact()));
    info.setLicense(convertLicense(infoAnnotation.license()));

    return info;
  }

  private License convertLicense(io.swagger.v3.oas.annotations.info.License licenseAnnotation) {
    License license = new License();

    if (StringUtils.isNotEmpty(licenseAnnotation.name())) {
      license.setName(licenseAnnotation.name());
    }
    if (StringUtils.isNotEmpty(licenseAnnotation.url())) {
      license.setUrl(licenseAnnotation.url());
    }

    if (StringUtils.isEmpty(license.getName()) && StringUtils.isEmpty(license.getUrl())) {
      return null;
    }

    return license;
  }

  private Contact convertContact(io.swagger.v3.oas.annotations.info.Contact contactAnnotation) {
    Contact contact = new Contact();

    if (StringUtils.isNotEmpty(contactAnnotation.name())) {
      contact.setName(contactAnnotation.name());
    }
    if (StringUtils.isNotEmpty(contactAnnotation.url())) {
      contact.setUrl(contactAnnotation.url());
    }
    if (StringUtils.isNotEmpty(contactAnnotation.email())) {
      contact.setEmail(contactAnnotation.email());
    }

    if (StringUtils.isEmpty(contact.getName()) &&
        StringUtils.isEmpty(contact.getUrl()) &&
        StringUtils.isEmpty(contact.getEmail())) {
      return null;
    }

    return contact;
  }

  private List<Tag> convertTags(io.swagger.v3.oas.annotations.tags.Tag[] tagArray) {
    if (tagArray == null) {
      return null;
    }

    List<Tag> tags = Arrays.stream(tagArray)
        .filter(t -> !t.name().isEmpty())
        .map(this::convertTag)
        .collect(Collectors.toList());
    return tags.isEmpty() ? null : tags;
  }

  private Tag convertTag(io.swagger.v3.oas.annotations.tags.Tag tagAnnotation) {
    Tag tag = new Tag();
    tag.setName(tagAnnotation.name());
    tag.setDescription(tagAnnotation.description());
    return tag;
  }
}
