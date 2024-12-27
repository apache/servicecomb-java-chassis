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

package org.apache.servicecomb.samples;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestSchema(schemaId = "ProviderController")
@RequestMapping(path = "/")
public class ProviderController implements InitializingBean {
    private Environment environment;

    @Autowired
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    // a very simple service to echo the request parameter
    @GetMapping("/sayHello")
    public String sayHello(@RequestParam("name") String name) {
//    return "Hello " + environment.getProperty("servicecomb.rest.address");
        return "Hello " + name;
    }

    @GetMapping("/getConfig")
    public String getConfig(@RequestParam("key") String key) {
        return environment.getProperty(key);
    }

    @PostMapping(path = "/testContentType", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User testContentType(@RequestBody User user) {
        return user;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
