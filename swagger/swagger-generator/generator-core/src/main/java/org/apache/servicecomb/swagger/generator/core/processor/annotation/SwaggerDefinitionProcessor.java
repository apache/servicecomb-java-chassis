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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.ClassAnnotationProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;

import io.swagger.annotations.Scope;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.In;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.util.BaseReaderUtils;

public class SwaggerDefinitionProcessor implements ClassAnnotationProcessor<SwaggerDefinition> {
  @Override
  public Type getProcessType() {
    return SwaggerDefinition.class;
  }

  @Override
  public void process(SwaggerGenerator swaggerGenerator, SwaggerDefinition definitionAnnotation) {
    Swagger swagger = swaggerGenerator.getSwagger();

    if (StringUtils.isNotEmpty(definitionAnnotation.basePath())) {
      swaggerGenerator.setBasePath(definitionAnnotation.basePath());
    }
    if (StringUtils.isNotEmpty(definitionAnnotation.host())) {
      swagger.setHost(definitionAnnotation.host());
    }

    SwaggerUtils.setConsumes(swagger, definitionAnnotation.consumes());
    SwaggerUtils.setProduces(swagger, definitionAnnotation.produces());
    swagger.setSchemes(convertSchemes(definitionAnnotation.schemes()));
    swagger.setTags(convertTags(definitionAnnotation.tags()));
    swagger.setSecurityDefinitions(convertSecurityDefinitions(definitionAnnotation.securityDefinition()));
    swagger.setInfo(convertInfo(definitionAnnotation.info()));
    swagger.setExternalDocs(convertExternalDocs(definitionAnnotation.externalDocs()));
  }

  private Info convertInfo(io.swagger.annotations.Info infoAnnotation) {
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
    info.getVendorExtensions().putAll(BaseReaderUtils.parseExtensions(infoAnnotation.extensions()));

    return info;
  }

  private License convertLicense(io.swagger.annotations.License licenseAnnotation) {
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

  private Contact convertContact(io.swagger.annotations.Contact contactAnnotation) {
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

  private List<Tag> convertTags(io.swagger.annotations.Tag[] tagArray) {
    if (tagArray == null) {
      return null;
    }

    List<Tag> tags = Arrays.stream(tagArray)
        .filter(t -> !t.name().isEmpty())
        .map(this::convertTag)
        .collect(Collectors.toList());
    return tags.isEmpty() ? null : tags;
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

    if (StringUtils.isNotEmpty(annotationExternalDocs.url())) {
      externalDocs.setUrl(annotationExternalDocs.url());
    }
    if (StringUtils.isNotEmpty(annotationExternalDocs.value())) {
      externalDocs.setDescription(annotationExternalDocs.value());
    }

    if (StringUtils.isEmpty(externalDocs.getUrl()) && StringUtils.isEmpty(externalDocs.getDescription())) {
      return null;
    }

    return externalDocs;
  }

  private List<Scheme> convertSchemes(SwaggerDefinition.Scheme[] schemeArray) {
    if (schemeArray == null) {
      return null;
    }

    return Arrays.stream(schemeArray)
        .map(this::convertScheme)
        .collect(Collectors.toList());
  }

  private Scheme convertScheme(io.swagger.annotations.SwaggerDefinition.Scheme annotationScheme) {
    if (SwaggerDefinition.Scheme.DEFAULT.equals(annotationScheme)) {
      return Scheme.HTTP;
    }
    return Scheme.forValue(annotationScheme.name());
  }

  private Map<String, SecuritySchemeDefinition> convertSecurityDefinitions(
      SecurityDefinition securityDefinition) {
    Map<String, SecuritySchemeDefinition> definitionMap = new LinkedHashMap<>();

    Arrays.stream(securityDefinition.oAuth2Definitions())
        .forEach(annotation -> addSecurityDefinition(definitionMap, annotation.key(), convertOAuth2(annotation)));
    Arrays.stream(securityDefinition.apiKeyAuthDefinitions())
        .forEach(annotation -> addSecurityDefinition(definitionMap, annotation.key(), convertApiKey(annotation)));
    Arrays.stream(securityDefinition.basicAuthDefinitions())
        .forEach(annotation -> addSecurityDefinition(definitionMap, annotation.key(), convertBasicAuth(annotation)));

    return definitionMap.isEmpty() ? null : definitionMap;
  }

  private void addSecurityDefinition(Map<String, SecuritySchemeDefinition> definitionMap,
      String key, SecuritySchemeDefinition definition) {
    if (StringUtils.isEmpty(key) || definition == null) {
      return;
    }

    definitionMap.put(key, definition);
  }

  private String emptyAsNull(@Nonnull String value) {
    return value.isEmpty() ? null : value;
  }

  private OAuth2Definition convertOAuth2(io.swagger.annotations.OAuth2Definition annotation) {
    OAuth2Definition definition = new OAuth2Definition();

    definition.setDescription(emptyAsNull(annotation.description()));
    definition.setFlow(emptyAsNull(annotation.flow().name()));
    definition.setAuthorizationUrl(emptyAsNull(annotation.authorizationUrl()));
    definition.setTokenUrl(emptyAsNull(annotation.tokenUrl()));
    for (Scope scope : annotation.scopes()) {
      if (StringUtils.isEmpty(scope.name())) {
        continue;
      }
      definition.addScope(scope.name(), scope.description());
    }

    if (definition.getDescription() == null
        && definition.getFlow() == null
        && definition.getAuthorizationUrl() == null
        && definition.getTokenUrl() == null
        && definition.getScopes() == null) {
      return null;
    }

    return definition;
  }

  private SecuritySchemeDefinition convertApiKey(io.swagger.annotations.ApiKeyAuthDefinition annotation) {
    if (StringUtils.isEmpty(annotation.name())) {
      return null;
    }

    ApiKeyAuthDefinition definition = new ApiKeyAuthDefinition();

    definition.setDescription(emptyAsNull(annotation.description()));
    definition.setIn(In.forValue(annotation.in().name()));
    definition.setName(annotation.name());

    return definition;
  }

  private SecuritySchemeDefinition convertBasicAuth(io.swagger.annotations.BasicAuthDefinition annotation) {
    if (annotation.description().isEmpty()) {
      return null;
    }

    BasicAuthDefinition definition = new BasicAuthDefinition();
    definition.setDescription(annotation.description());
    return definition;
  }
}
