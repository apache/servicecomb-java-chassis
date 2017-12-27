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

package io.servicecomb.metrics.sample.writefile.log4j2config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.springframework.stereotype.Component;

@Component
public class Log4j2ConfigInitializer {

  public Log4j2ConfigInitializer() {
    ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

    builder.setStatusLevel(Level.ERROR);
    builder.setConfigurationName("MetricsConfig");
// create a rolling file appender
    LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout")
        .addAttribute("pattern", "%m%n");
    ComponentBuilder triggeringPolicy = builder.newComponent("Policies")
        .addComponent(builder.newComponent("CronTriggeringPolicy").addAttribute("schedule", "0 0 0 * * ?"))
        .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "10MB"));
    AppenderComponentBuilder appenderBuilder = builder.newAppender("rolling", "RollingFile")
        .addAttribute("fileName", "target/rolling.log")
        .addAttribute("filePattern", "target/archive/rolling-%d{MM-dd-yy}.log.gz")
        .add(layoutBuilder)
        .addComponent(triggeringPolicy);
    builder.add(appenderBuilder);

// create the new logger
    builder.add(builder.newLogger("TestLogger", Level.DEBUG)
        .add(builder.newAppenderRef("rolling"))
        .addAttribute("additivity", false));

    builder.add(builder.newRootLogger(Level.DEBUG)
        .add(builder.newAppenderRef("rolling")));
    Configurator.initialize(builder.build());
  }


}
