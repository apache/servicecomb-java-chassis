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
package org.apache.servicecomb.swagger.generator.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.servicecomb.swagger.generator.SwaggerConst;

import jakarta.ws.rs.core.MediaType;

/**
 * Context information to help generate specific open api parts.
 */
public class SwaggerGeneratorContext {
  protected static final List<String> SUPPORTED_CONTENT_TYPE
      = Arrays.asList(MediaType.APPLICATION_JSON, SwaggerConst.PROTOBUF_TYPE, MediaType.TEXT_PLAIN,
      MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED);

  protected static final List<String> SUPPORTED_BODY_CONTENT_TYPE
      = Arrays.asList(MediaType.APPLICATION_JSON, SwaggerConst.PROTOBUF_TYPE, MediaType.TEXT_PLAIN);

  protected static final List<String> SUPPORTED_FORM_CONTENT_TYPE
      = Arrays.asList(MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_FORM_URLENCODED);

  protected List<String> supportedConsumes;

  protected List<String> supportedProduces;

  public SwaggerGeneratorContext() {
    supportedConsumes = new ArrayList<>();
    supportedConsumes.addAll(SUPPORTED_CONTENT_TYPE);
    supportedProduces = new ArrayList<>();
    supportedProduces.addAll(SUPPORTED_CONTENT_TYPE);
  }

  public SwaggerGeneratorContext(SwaggerGeneratorContext parent) {
    supportedConsumes = new ArrayList<>();
    supportedConsumes.addAll(parent.supportedConsumes);
    supportedProduces = new ArrayList<>();
    supportedProduces.addAll(parent.supportedProduces);
  }

  public void updateProduces(List<String> produces) {
    checkMediaTypeValid(produces);
    supportedProduces.clear();
    supportedProduces.addAll(produces);
  }

  public void updateConsumes(List<String> consumes) {
    checkMediaTypeValid(consumes);
    supportedConsumes.clear();
    supportedConsumes.addAll(consumes);
  }

  public List<String> getSupportedConsumes() {
    return supportedConsumes;
  }

  public List<String> getSupportedProduces() {
    return supportedProduces;
  }

  private void checkMediaTypeValid(List<String> produces) {
    for (String produce : produces) {
      if (!SUPPORTED_CONTENT_TYPE.contains(produce)) {
        throw new IllegalArgumentException("Not support media type " + produce);
      }
    }
  }
}
