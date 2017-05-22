/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.springboot.starter.transport;

import io.servicecomb.transport.rest.servlet.RestServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Cse embedded servlet class.
 *
 * @author
 *
 * @version
 */
@Configuration
public class CseEmbeddedServlet {

    @Autowired
    private ConfigurableEnvironment env;

    private static final String DEFAULT_SERVLET_NAME = "RestServlet";

    private static final String DEFAULT_URL = "/pojo/rest/*";

    @Bean
    public ServletRegistrationBean restServletRegistration() {
        String name = env.getProperty("cse.servlet.name");
        name = name == null ? DEFAULT_SERVLET_NAME : name;
        String url = env.getProperty("cse.servlet.url");
        url = url == null ? DEFAULT_URL : url;
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(new RestServlet(), url);
        registrationBean.setName(name);
        return registrationBean;
    }

}
