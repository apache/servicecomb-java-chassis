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

package org.apache.servicecomb.foundation.common.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ResourceUtil {

  private ResourceUtil() {
  }

  /**
   * Search the specified location in classpath, and returns the resources with the specified suffix.
   */
  public static List<URI> findResourcesBySuffix(String resourceLocation, String fileNameSuffix)
      throws IOException, URISyntaxException {
    return findResources(resourceLocation, path -> path.toString().endsWith(fileNameSuffix));
  }

  /**
   * Search the specified location in classpath, all the resources found are collected and returned.
   */
  public static List<URI> findResources(String resourceLocation) throws IOException, URISyntaxException {
    return findResources(resourceLocation, p -> true);
  }

  /**
   * Search the specified location in classpath, which can be a directory or the exact file location,
   * and returns a list of URIs pointing to the matched resources.
   *
   * @param resourceLocation in which location the resources are searched
   * @param filter to pick out those matched resources
   */
  public static List<URI> findResources(String resourceLocation, Predicate<Path> filter)
      throws IOException, URISyntaxException {
    ArrayList<URI> result = new ArrayList<>();

    Enumeration<URL> dirURLs = JvmUtils.findClassLoader().getResources(resourceLocation);
    while (dirURLs.hasMoreElements()) {
      URL dirURL = dirURLs.nextElement();

      if (dirURL.getProtocol().equals("file")) {
        Path dirPath = Paths.get(dirURL.toURI());
        collectResourcesFromPath(dirPath, filter, result);
        continue;
      }

      try (FileSystem fileSystem = FileSystems.newFileSystem(dirURL.toURI(), Collections.emptyMap())) {
        Path dirPath = fileSystem.getPath(resourceLocation);
        collectResourcesFromPath(dirPath, filter, result);
      }
    }

    return result;
  }

  private static void collectResourcesFromPath(Path path, Predicate<Path> filter, Collection<URI> resources)
      throws IOException {
    try (Stream<Path> dirContentTraversalStream = Files.walk(path)) {
      dirContentTraversalStream
          .filter(filter)
          .map(Path::toUri)
          .forEach(resources::add);
    }
  }
}
