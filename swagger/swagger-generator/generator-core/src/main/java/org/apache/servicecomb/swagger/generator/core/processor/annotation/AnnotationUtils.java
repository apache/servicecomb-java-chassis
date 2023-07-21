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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.SwaggerConst;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MediaType;

/**
 * Utility class to convert from OpenAPI annotations to models.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class AnnotationUtils {
  private AnnotationUtils() {

  }

  public static List<io.swagger.v3.oas.models.servers.Server> serversModel(Server[] servers) {
    if (servers == null) {
      return null;
    }
    return Arrays.stream(servers).map(AnnotationUtils::convertServer).collect(Collectors.toList());
  }

  public static io.swagger.v3.oas.models.servers.Server convertServer(Server server) {
    io.swagger.v3.oas.models.servers.Server item = new io.swagger.v3.oas.models.servers.Server();
    item.setUrl(server.url());
    item.setDescription(server.description());
    return item;
  }

  public static io.swagger.v3.oas.models.info.Info infoModel(Info infoAnnotation) {
    if (infoAnnotation == null) {
      return null;
    }

    io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info();

    info.setTitle(infoAnnotation.title());
    info.setVersion(infoAnnotation.version());
    if (StringUtils.isNotEmpty(infoAnnotation.description())) {
      info.setDescription(infoAnnotation.description());
    }
    if (StringUtils.isNotEmpty(infoAnnotation.termsOfService())) {
      info.setTermsOfService(infoAnnotation.termsOfService());
    }
    info.setContact(contactModel(infoAnnotation.contact()));
    info.setLicense(licenseModel(infoAnnotation.license()));
    info.setExtensions(extensionsModel(infoAnnotation.extensions()));

    return info;
  }

  public static io.swagger.v3.oas.models.info.License licenseModel(License licenseAnnotation) {
    io.swagger.v3.oas.models.info.License license = new io.swagger.v3.oas.models.info.License();

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

  public static io.swagger.v3.oas.models.info.Contact contactModel(Contact contactAnnotation) {
    io.swagger.v3.oas.models.info.Contact contact = new io.swagger.v3.oas.models.info.Contact();

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

  public static List<io.swagger.v3.oas.models.tags.Tag> tagsModel(Tag[] tagArray) {
    if (tagArray == null) {
      return null;
    }

    List<io.swagger.v3.oas.models.tags.Tag> tags = Arrays.stream(tagArray)
        .filter(t -> !t.name().isEmpty())
        .map(AnnotationUtils::tagModel)
        .collect(Collectors.toList());
    return tags.isEmpty() ? null : tags;
  }

  public static List<String> tagsModel(String[] tagArray) {
    if (tagArray == null) {
      return null;
    }

    List<String> tags = Arrays.stream(tagArray)
        .filter(t -> !t.isEmpty())
        .collect(Collectors.toList());
    return tags.isEmpty() ? null : tags;
  }

  public static io.swagger.v3.oas.models.tags.Tag tagModel(Tag tagAnnotation) {
    io.swagger.v3.oas.models.tags.Tag tag = new io.swagger.v3.oas.models.tags.Tag();
    tag.setName(tagAnnotation.name());
    tag.setDescription(tagAnnotation.description());
    tag.setExternalDocs(externalDocumentationModel(tagAnnotation.externalDocs()));
    tag.setExtensions(extensionsModel(tagAnnotation.extensions()));
    return tag;
  }

  public static io.swagger.v3.oas.models.ExternalDocumentation externalDocumentationModel(
      ExternalDocumentation externalDocs) {
    io.swagger.v3.oas.models.ExternalDocumentation doc = new io.swagger.v3.oas.models.ExternalDocumentation();
    doc.setUrl(externalDocs.url());
    doc.setDescription(externalDocs.description());
    return doc;
  }

  public static Map<String, Object> extensionsModel(Extension[] extensions) {
    Map<String, Object> result = new HashMap<>();
    Stream.of(extensions).forEach(e -> result.put(e.name(), extensionPropertiesModel(e.properties())));
    return result;
  }

  public static Map<String, String> extensionPropertiesModel(ExtensionProperty[] properties) {
    Map<String, String> result = new HashMap<>();
    Stream.of(properties).forEach(e -> result.put(e.name(), e.value()));
    return result;
  }

  public static String responseCodeModel(ApiResponse apiResponse) {
    if (StringUtils.isEmpty(apiResponse.responseCode())) {
      return "200";
    }
    return apiResponse.responseCode();
  }

  public static io.swagger.v3.oas.models.responses.ApiResponses apiResponsesModel(OpenAPI openAPI,
      ApiResponses apiResponses) {
    io.swagger.v3.oas.models.responses.ApiResponses result =
        new io.swagger.v3.oas.models.responses.ApiResponses();
    result.setExtensions(extensionsModel(apiResponses.extensions()));
    for (ApiResponse apiResponse : apiResponses.value()) {
      result.addApiResponse(responseCodeModel(apiResponse), apiResponseModel(openAPI, apiResponse));
    }
    return result;
  }

  public static io.swagger.v3.oas.models.responses.ApiResponses apiResponsesModel(OpenAPI openAPI,
      ApiResponse[] apiResponses) {
    io.swagger.v3.oas.models.responses.ApiResponses result =
        new io.swagger.v3.oas.models.responses.ApiResponses();
    for (ApiResponse apiResponse : apiResponses) {
      if (result.get(responseCodeModel(apiResponse)) != null) {
        throw new IllegalStateException("not support too many ApiResponse with same status code");
      } else {
        result.addApiResponse(responseCodeModel(apiResponse), apiResponseModel(openAPI, apiResponse));
      }
    }
    return result;
  }

  public static io.swagger.v3.oas.models.responses.ApiResponse apiResponseModel(OpenAPI openAPI,
      ApiResponse apiResponse) {
    io.swagger.v3.oas.models.responses.ApiResponse result =
        new io.swagger.v3.oas.models.responses.ApiResponse();
    result.setDescription(apiResponse.description());
    result.setContent(contentModel(openAPI, apiResponse.content()));
    result.setHeaders(headersModel(openAPI, apiResponse.headers()));
    return result;
  }

  public static Map<String, io.swagger.v3.oas.models.headers.Header> headersModel(OpenAPI openAPI, Header[] headers) {
    Map<String, io.swagger.v3.oas.models.headers.Header> result = new HashMap<>();
    for (Header header : headers) {
      io.swagger.v3.oas.models.headers.Header model =
          new io.swagger.v3.oas.models.headers.Header();
      model.setDescription(header.description());
      model.setSchema(schemaModel(openAPI, header.schema()));
      result.put(header.name(), model);
    }
    return result;
  }

  public static io.swagger.v3.oas.models.media.Content contentModel(OpenAPI openAPI, Content[] contents) {
    io.swagger.v3.oas.models.media.Content result = new io.swagger.v3.oas.models.media.Content();
    for (io.swagger.v3.oas.annotations.media.Content content : contents) {
      String mediaTypeName = mediaTypeModel(content);
      MediaType mediaType = result.get(mediaTypeName);
      boolean isForm = SwaggerConst.FORM_MEDIA_TYPE.equals(mediaTypeName) ||
          SwaggerConst.FILE_MEDIA_TYPE.equals(mediaTypeName);
      if (mediaType == null) {
        mediaType = new MediaType();
        if (isForm) {
          io.swagger.v3.oas.models.media.Schema schema = new io.swagger.v3.oas.models.media.Schema();
          schema.setProperties(new HashMap<>());
          schema.addProperty(content.schema().name(), schemaModel(openAPI, content.schema(), content.examples()));
          mediaType.setSchema(schema);
        } else {
          mediaType.setSchema(schemaModel(openAPI, content.schema(), content.examples()));
        }
        result.addMediaType(mediaTypeName, mediaType);
      } else {
        if (isForm) {
          mediaType.getSchema().addProperty(content.schema().name(),
              schemaModel(openAPI, content.schema(), content.examples()));
        } else {
          throw new IllegalStateException("Not allowed to define duplicated content type for " + mediaTypeName);
        }
      }
    }
    return result;
  }

  public static io.swagger.v3.oas.models.parameters.RequestBody requestBodyModel(OpenAPI openAPI,
      RequestBody requestBody) {
    if (requestBody == null || isOperationDefaultRequestBody(requestBody)) {
      return null;
    }
    io.swagger.v3.oas.models.parameters.RequestBody result = new io.swagger.v3.oas.models.parameters.RequestBody();
    result.setContent(AnnotationUtils.contentModel(openAPI, requestBody.content()));
    return result;
  }

  private static boolean isOperationDefaultRequestBody(RequestBody requestBody) {
    return "".equals(requestBody.description()) && requestBody.content().length == 0
        && !requestBody.required() && requestBody.extensions().length == 0;
  }

  private static String mediaTypeModel(io.swagger.v3.oas.annotations.media.Content content) {
    if (StringUtils.isEmpty(content.mediaType())) {
      return SwaggerConst.DEFAULT_MEDIA_TYPE;
    }
    return content.mediaType();
  }

  public static io.swagger.v3.oas.models.media.Schema schemaModel(OpenAPI openAPI, Schema schema) {
    if (schema.implementation() != Void.class) {
      io.swagger.v3.oas.models.media.Schema result =
          SwaggerUtils.resolveTypeSchemas(openAPI, schema.implementation());
      result.setDescription(schema.description());
      result.setExample(schema.example());
      result.setNullable(schema.nullable());
      return result;
    }

    io.swagger.v3.oas.models.media.Schema result =
        new io.swagger.v3.oas.models.media.Schema();
    result.setType(schema.type());
    result.setFormat(schema.format());
    result.setDescription(schema.description());
    result.setExample(schema.example());
    result.setNullable(schema.nullable());
    return result;
  }

  public static io.swagger.v3.oas.models.media.Schema schemaModel(OpenAPI openAPI, Schema schema,
      ExampleObject[] exampleObjects) {
    io.swagger.v3.oas.models.media.Schema result = schemaModel(openAPI, schema);
    List<Object> examples = new ArrayList<>();
    for (ExampleObject exampleObject : exampleObjects) {
      examples.add(exampleObject.name() + ":" + exampleObject.value());
    }
    result.setExamples(examples);
    return result;
  }

  public static io.swagger.v3.oas.models.Operation operationModel(OpenAPI openAPI, Operation apiOperationAnnotation) {
    io.swagger.v3.oas.models.Operation result = new io.swagger.v3.oas.models.Operation();
    result.setSummary(apiOperationAnnotation.summary());
    result.setDescription(apiOperationAnnotation.description());
    result.setExtensions(extensionsModel(apiOperationAnnotation.extensions()));
    result.setResponses(apiResponsesModel(openAPI, apiOperationAnnotation.responses()));
    result.setOperationId(apiOperationAnnotation.operationId());
    result.setTags(tagsModel(apiOperationAnnotation.tags()));
    result.setRequestBody(requestBodyModel(openAPI, apiOperationAnnotation.requestBody()));
    return result;
  }
}
