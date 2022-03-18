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

package org.apache.servicecomb.demo.multiServiceCenterServerB;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.netflix.config.DynamicPropertyFactory;

@RestSchema(schemaId = "ConfigurationEndpoint")
@RequestMapping(path = "/register/url/config", produces = MediaType.APPLICATION_JSON)
public class ConfigurationEndpoint {
  private static final Logger LOGGER
      = LoggerFactory.getLogger(ServerEndpoint.class);

  private Environment environment;

  @Autowired
  public ConfigurationEndpoint(Environment environment) {
    this.environment = environment;
  }

  @Value("${demo.multi.service.center.serverB.key1}")
  private String key1;

  @Value("${demo.multi.service.center.serverB.key2}")
  private String key2;

  @Value("${demo.multi.service.center.serverB.key3}")
  private String key3;

  @Value("${demo.multi.service.center.serverB.key4}")
  private String key4;

  @Value("${demo.multi.service.center.serverB.key5}")
  private String key5;

  @Value("${demo.multi.service.center.serverB.key6}")
  private String key6;

  @Value("${demo.multi.service.center.serverB.key7}")
  private List<String> key7;

  @GetMapping(path = "/config")
  public String getValue(@RequestParam(name = "key") String key, @RequestParam(name = "type") int type) {
    if (type == 1) {
      return environment.getProperty(key);
    } else if (type == 2) {
      return DynamicPropertyFactory.getInstance().getStringProperty(key, null).get();
    } else {
      switch (key) {
        case "demo.multi.service.center.serverB.key1":
          return key1;
        case "demo.multi.service.center.serverB.key2":
          return key2;
        case "demo.multi.service.center.serverB.key3":
          return key3;
        case "demo.multi.service.center.serverB.key4":
          return key4;
        case "demo.multi.service.center.serverB.key5":
          return key5;
        case "demo.multi.service.center.serverB.key6":
          return key6;
        default:
          return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  @GetMapping(path = "/configList")
  public List<String> getValueList(@RequestParam(name = "key") String key, @RequestParam(name = "type") int type) {
    if (type == 1) {
      return environment.getProperty(key, List.class);
    } else {
      switch (key) {
        case "demo.multi.service.center.serverB.key7":
          return key7;
        default:
          return null;
      }
    }
  }
}
