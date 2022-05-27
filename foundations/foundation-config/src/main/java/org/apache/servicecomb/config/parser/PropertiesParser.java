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

package org.apache.servicecomb.config.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesParser implements Parser {
  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesParser.class);

  @Override
  public Map<String, Object> parse(String content, String prefix, boolean addPrefix) {
    Properties properties = new Properties();
    try {
      properties.load(new StringReader(content));
    } catch (IOException e) {
      LOGGER.error("parse properties content failed, message={}", e.getMessage());
    }
    return Parser.propertiesToMap(properties, prefix, addPrefix);
  }
}
