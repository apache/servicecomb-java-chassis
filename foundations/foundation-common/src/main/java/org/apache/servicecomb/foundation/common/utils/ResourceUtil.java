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
import java.util.function.Function;
import java.util.stream.Stream;

public class ResourceUtil {
  /**
   * Search the specified directories in classpath, and returns a list of URIs pointing to the matched resources.
   *
   * @param directory in which directory the resources are collected
   * @param filter to pick out those matched resources
   */
  public static List<URI> loadResources(String directory, Function<Path, Boolean> filter)
      throws IOException, URISyntaxException {
    ArrayList<URI> result = new ArrayList<>();

    Enumeration<URL> dirURLs = JvmUtils.findClassLoader().getResources(directory);
    while (dirURLs.hasMoreElements()) {
      URL dirURL = dirURLs.nextElement();
      if (dirURL.getProtocol().equals("file")) {
        Path dirPath = Paths.get(dirURL.toURI());
        collectYamlFromPath(dirPath, filter, result);
        continue;
      }

      try (FileSystem fileSystem = FileSystems.newFileSystem(dirURL.toURI(), Collections.emptyMap())) {
        Path dirPath = fileSystem.getPath(directory);
        collectYamlFromPath(dirPath, filter, result);
      }
    }

    return result;
  }

  /**
   * A convenient method to get a filter to match the resource files by file path suffix.
   */
  public static Function<Path, Boolean> matchSuffix(String suffix) {
    return path -> path.toString().endsWith(suffix);
  }

  private static void collectYamlFromPath(Path dirPath, Function<Path, Boolean> filter, Collection<URI> container)
      throws IOException {
    try (Stream<Path> dirContentTraversalStream = Files.walk(dirPath)) {
      dirContentTraversalStream
          .filter(filter::apply)
          .map(Path::toUri)
          .forEach(container::add);
    }
  }
}
