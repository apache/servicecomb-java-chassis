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

package io.servicecomb.config.archaius.sources;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.servicecomb.config.ConfigMapping;
import io.servicecomb.config.YAMLUtil;
import com.netflix.config.PollResult;
import com.netflix.config.PolledConfigurationSource;

public class YAMLConfigurationSource implements PolledConfigurationSource {

    private URL[] configUrls = new URL[0];

    private static final String CONFIG_URL = "cse.configurationSource.additionalUrls";

    /**
     * Default configuration file name to be used by default constructor. This file should
     * be on the classpath. The file name can be overridden by the value of system property
     * <code>configurationSource.defaultFileName</code>
     */
    private static final String DEFAULT_CONFIG_FILE_NAME = "microservice.yaml";

    private static final String DEFAULT_CONFIG_FILE_FROM_CLASSPATH =
        System.getProperty("cse.configurationSource.defaultFileName") == null ? DEFAULT_CONFIG_FILE_NAME
                : System.getProperty("cse.configurationSource.defaultFileName");

    private static final Logger LOGGER = LoggerFactory.getLogger(YAMLConfigurationSource.class);

    private static URL[] createUrls(String... urlStrings) {
        if (urlStrings == null || urlStrings.length == 0) {
            throw new IllegalArgumentException("urlStrings is null or empty");
        }
        URL[] urls = new URL[urlStrings.length];

        try {
            for (int i = 0; i < urlStrings.length; i++) {
                urls[i] = new URL(urlStrings[i]);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return urls;
    }

    public YAMLConfigurationSource() {
        List<URL> urlList = new ArrayList<URL>();
        // add classpath urls
        URL configFromClasspath = getConfigFileFromClasspath();
        if (configFromClasspath != null) {
            urlList.add(configFromClasspath);
        }

        // add property specified files
        String[] fileNames = getDefaultFileSources();
        if (fileNames.length != 0) {
            urlList.addAll(Arrays.asList(createUrls(fileNames)));
        }

        if (urlList.size() == 0) {
            LOGGER.warn("No URLs will be polled as dynamic configuration sources.");
            LOGGER.info("To enable URLs as dynamic configuration sources, define System property "
                    + CONFIG_URL + " or make " + DEFAULT_CONFIG_FILE_FROM_CLASSPATH + " available on classpath.");
        } else {
            if (configUrls.length == 0) {
                configUrls = urlList.toArray(new URL[urlList.size()]);
            } else {
                configUrls = YAMLUtil.arrayConcat(configUrls, urlList.toArray(new URL[urlList.size()]));
            }
        }
    }

    public YAMLConfigurationSource(File... fileNames) {
        this();
        if (configUrls.length == 0) {
            configUrls = getConfigFileFromPath(fileNames);
        } else {
            configUrls = YAMLUtil.arrayConcat(configUrls, getConfigFileFromPath(fileNames));
        }
    }

    @SuppressWarnings("deprecation")
    private URL[] getConfigFileFromPath(File... fileNames) {
        URL[] urls = new URL[fileNames.length];

        for (int i = 0; i < fileNames.length; i++) {
            try {
                urls[i] = fileNames[i].toURL();
            } catch (MalformedURLException e) {
                LOGGER.warn("can not read config file from source:" + fileNames[i]);
            }
        }

        return urls;
    }

    private URL getConfigFileFromClasspath() {
        URL url = null;
        // attempt to load from the context classpath
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            url = loader.getResource(DEFAULT_CONFIG_FILE_FROM_CLASSPATH);
        }
        if (url == null) {
            // attempt to load from the system classpath
            url = ClassLoader.getSystemResource(DEFAULT_CONFIG_FILE_FROM_CLASSPATH);
        }
        if (url == null) {
            // attempt to load from the system classpath
            url = YAMLConfigurationSource.class.getResource(DEFAULT_CONFIG_FILE_FROM_CLASSPATH);
        }
        return url;
    }

    public List<URL> getConfigUrls() {
        return Collections.unmodifiableList(Arrays.asList(configUrls));
    }

    private static String[] getDefaultFileSources() {
        String name = System.getProperty(CONFIG_URL);
        String[] fileNames;
        if (name != null) {
            fileNames = name.split(",");
        } else {
            fileNames = new String[0];
        }
        return fileNames;
    }

    public PollResult poll(boolean b, Object o) throws Exception {
        Map<String, Object> configurations = new LinkedHashMap<String, Object>();

        for (URL url : configUrls) {
            try (InputStream in = url.openStream()) {
                configurations.putAll(YAMLUtil.yaml2Properties(in));
            }
        }

        configurations = ConfigMapping.getConvertedMap(configurations);
        return PollResult.createFull(configurations);
    }
}
