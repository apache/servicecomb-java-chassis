/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.transport.rest.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.servicecomb.foundation.common.exceptions.ServiceCombException;

public class RestServletInjector {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestServletInjector.class);

    public static final String SERVLET_NAME = "ServicecombRestServlet";

    public static Dynamic defaultInject(ServletContext servletContext) {
        RestServletInjector injector = new RestServletInjector();

        String urlPattern = ServletConfig.getServletUrlPattern();
        return injector.inject(servletContext, urlPattern);
    }

    public Dynamic inject(ServletContext servletContext, String urlPattern) {
        if (StringUtils.isEmpty(urlPattern)) {
            LOGGER.warn("urlPattern is empty, ignore register {}.", SERVLET_NAME);
            return null;
        }

        String listenAddress = ServletConfig.getLocalServerAddress();
        if (!ServletUtils.canPublishEndpoint(listenAddress)) {
            LOGGER.warn("ignore register {}.", SERVLET_NAME);
            return null;
        }

        checkUrlPattern(urlPattern);

        // dynamic deploy a servlet to handle serviceComb RESTful request
        Dynamic dynamic = servletContext.addServlet(SERVLET_NAME, RestServlet.class);
        dynamic.setAsyncSupported(true);
        dynamic.addMapping(urlPattern);
        dynamic.setLoadOnStartup(0);
        LOGGER.info("RESTful servlet url pattern: {}.", urlPattern);

        return dynamic;
    }

    // we only support path prefix rule, and only one path, it's what servicecomb RESTful request want.
    // so only check if sidechar is the last char
    // eg: *.xxx is a invalid urlPattern
    // other invalid urlPattern will be check by web container, we do not handle that
    void checkUrlPattern(String urlPattern) {
        if (urlPattern.indexOf('\n') > 0) {
            throw new ServiceCombException("not support multiple path rule.");
        }

        if (!urlPattern.startsWith("/")) {
            throw new ServiceCombException("only support rule like /* or /path/* or /path1/path2/* and so on.");
        }

        int idx = urlPattern.indexOf("/*");
        if (idx < 0 || (idx >= 0 && idx != urlPattern.length() - 2)) {
            throw new ServiceCombException("only support rule like /* or /path/* or /path1/path2/* and so on.");
        }
    }
}
