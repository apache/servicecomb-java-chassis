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

package org.apache.servicecomb.config.archaius.sources;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class TestMicroserviceConfigLoader {

  private final MicroserviceConfigLoader loader = new MicroserviceConfigLoader();

  private ConfigModel createConfigModel(String protocol, int order, String file) throws MalformedURLException {
    ConfigModel configModel = new ConfigModel();
    configModel.setUrl(new URL(protocol, null, file));
    configModel.setOrder(order);
    return configModel;
  }

  @Test
  public void configsSortedByInsertionOrder() throws MalformedURLException {
    loader.getConfigModels().add(createConfigModel("jar", 0, "c"));
    loader.getConfigModels().add(createConfigModel("jar", 0, "b"));
    loader.getConfigModels().add(createConfigModel("jar", 0, "a"));

    loader.sort();

    assertEquals(urls("jar:c", "jar:b", "jar:a"), urlsOf(loader.getConfigModels()));
  }

  @Test
  public void configsSortedBySpecifiedOrder() throws MalformedURLException {
    loader.getConfigModels().add(createConfigModel("jar", 1, "b"));
    loader.getConfigModels().add(createConfigModel("jar", -10, "c"));
    loader.getConfigModels().add(createConfigModel("jar", Integer.MAX_VALUE, "a"));

    loader.sort();

    assertEquals(urls("jar:c", "jar:b", "jar:a"), urlsOf(loader.getConfigModels()));
  }

  @Test
  public void jarsAlwaysHaveHigherPriorityThanFiles() throws MalformedURLException {
    loader.getConfigModels().add(createConfigModel("file", 0, "f2"));
    loader.getConfigModels().add(createConfigModel("jar", 1, "j1"));
    loader.getConfigModels().add(createConfigModel("file", 0, "f1"));

    loader.sort();

    assertEquals(urls("jar:j1", "file:f2", "file:f1"), urlsOf(loader.getConfigModels()));
  }

  private String urlsOf(List<ConfigModel> configModels) {
    return String.join(",",
        configModels
            .stream()
            .map(configModel -> configModel.getUrl().toString())
            .collect(Collectors.toList()));
  }

  private String urls(String... urls) {
    return String.join(",", (CharSequence[]) urls);
  }

  @Test
  public void testLoadEmptyYaml() throws IOException {
    loader.load("empty.yaml");
    Assert.assertTrue(loader.getConfigModels().get(0).getConfig().isEmpty());
  }

  @Test
  public void testLoadNotExistYaml() throws IOException {
    URL url = URI.create("file:/notExist.yaml").toURL();
    try {
      loader.load(url);
      Assert.fail("must throw exception");
    } catch (FileNotFoundException e) {
      Assert.assertTrue(true);
    }
  }
}
