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
package org.apache.servicecomb.authentication.provider;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.apache.servicecomb.swagger.SwaggerUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestPathCheckUtils {
  private static final String KEY_INCLUDE_PATH = "servicecomb.publicKey.accessControl.includePathPatterns";

  private static final String KEY_EXCLUDE_PATH = "servicecomb.publicKey.accessControl.excludePathPatterns";

  private static final String BASE_PATH = "/api/v1";

  private Environment environment;

  private OpenAPI swagger;

  @BeforeEach
  public void setUp() {
    environment = Mockito.mock(Environment.class);
    swagger = new OpenAPI();
    swagger.setServers(new ArrayList<>());
    swagger.getServers().add(new Server().url(BASE_PATH));
  }

  @Test
  public void testExcludePathWithBasePathAndExactMatch() {
    String operationPath = "/public";
    when(environment.getProperty(KEY_EXCLUDE_PATH, "")).thenReturn(BASE_PATH + operationPath);

    String fullPath = SwaggerUtils.concatAbsolutePath(swagger, operationPath);
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should not require auth for excluded path with exact match");
  }

  @Test
  public void testExcludePathWithBasePathAndWildcard() {
    when(environment.getProperty(KEY_EXCLUDE_PATH, "")).thenReturn(BASE_PATH + "/public/*");

    String fullPath = SwaggerUtils.concatAbsolutePath(swagger, "/public/test");
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should not require auth for excluded path with wildcard");

    fullPath = SwaggerUtils.concatAbsolutePath(swagger, "/public/nested/path");
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should not require auth for excluded nested path with wildcard");
  }

  @Test
  public void testIncludePathWithBasePath() {
    when(environment.getProperty(KEY_EXCLUDE_PATH, "")).thenReturn("");
    when(environment.getProperty(KEY_INCLUDE_PATH, "")).thenReturn(BASE_PATH + "/private/*");

    String fullPath = SwaggerUtils.concatAbsolutePath(swagger, "/private/resource");
    assertFalse(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should require auth for included path");

    fullPath = SwaggerUtils.concatAbsolutePath(swagger, "/public/resource");
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should not require auth for non-included path");
  }

  @Test
  public void testExcludeOverrideIncludePath() {
    when(environment.getProperty(KEY_EXCLUDE_PATH, "")).thenReturn(BASE_PATH + "/resource");
    when(environment.getProperty(KEY_INCLUDE_PATH, "")).thenReturn(BASE_PATH + "/*");

    String fullPath = SwaggerUtils.concatAbsolutePath(swagger, "/resource");
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Exclude patterns should override include patterns");
  }

  @Test
  public void testMultipleExcludePaths() {
    List<String> operationPaths = List.of("/public", "/health", "/metrics/*");
    when(environment.getProperty(KEY_EXCLUDE_PATH, "")).thenReturn(concatPath(BASE_PATH, operationPaths));

    String fullPath = SwaggerUtils.concatAbsolutePath(swagger, "/public");
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should not require auth for first exclude path");

    fullPath = SwaggerUtils.concatAbsolutePath(swagger, "/health");
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should not require auth for second exclude path");

    fullPath = SwaggerUtils.concatAbsolutePath(swagger, "/metrics/jvm");
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should not require auth for wildcard exclude path");
  }

  @Test
  public void testDifferentBasePath() {
    String basePath = "/different/base";
    String publicOperationPath = "/public";
    String privateOperationPath = "/private";
    swagger.getServers().clear();
    swagger.getServers().add(new Server().url(basePath));
    when(environment.getProperty(KEY_EXCLUDE_PATH, "")).thenReturn(basePath + publicOperationPath);

    String fullPath = SwaggerUtils.concatAbsolutePath(swagger, publicOperationPath);
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should not require auth with different base path");

    fullPath = SwaggerUtils.concatAbsolutePath(swagger, privateOperationPath);
    assertFalse(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should require auth for non-excluded path with different base path");
  }

  @Test
  public void testNoBasePath() {
    String operationPath = "/public";
    swagger.setServers(null);
    when(environment.getProperty(KEY_EXCLUDE_PATH, "")).thenReturn(operationPath);

    String fullPath = SwaggerUtils.concatAbsolutePath(swagger, operationPath);
    assertTrue(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should not require auth when no base path is set");
  }

  @Test
  public void testEmptyConfiguration() {
    when(environment.getProperty(KEY_EXCLUDE_PATH, "")).thenReturn("");
    when(environment.getProperty(KEY_INCLUDE_PATH, "")).thenReturn("");

    String fullPath = SwaggerUtils.concatAbsolutePath(swagger, "/any/path");
    assertFalse(PathCheckUtils.isNotRequiredAuth(fullPath, environment),
        "Should require auth by default when no patterns are configured");
  }

  private String concatPath(String basePath, List<String> paths) {
    return paths.stream().map(path -> basePath + path).collect(Collectors.joining(","));
  }
}
