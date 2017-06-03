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

package io.servicecomb.transport.rest.servlet;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;

import io.servicecomb.foundation.common.utils.BeanUtils;
import io.servicecomb.foundation.common.utils.Log4jUtils;

public class RestServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            initLog();
            initSpring(sce);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public void initLog() throws Exception {
        Log4jUtils.init();
    }

    public void initSpring(ServletContextEvent sce) {
        String locations = sce.getServletContext().getInitParameter("contextConfigLocation");
        String[] locationArray = splitLocations(locations);
        BeanUtils.init(locationArray);
    }

    public String[] splitLocations(String locations) {
        if (StringUtils.isEmpty(locations)) {
            return new String[] {BeanUtils.DEFAULT_BEAN_RESOURCE};
        }

        Set<String> locationSet = new LinkedHashSet<>();
        for (String location : locations.split("[\r\n]")) {
            location = location.trim();
            if (StringUtils.isEmpty(location)) {
                continue;
            }

            locationSet.add(location);
        }
        locationSet.add(BeanUtils.DEFAULT_BEAN_RESOURCE);

        return locationSet.toArray(new String[locationSet.size()]);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
