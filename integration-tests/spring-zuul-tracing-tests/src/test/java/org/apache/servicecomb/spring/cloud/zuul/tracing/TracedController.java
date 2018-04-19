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

package org.apache.servicecomb.spring.cloud.zuul.tracing;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.lang.invoke.MethodHandles;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "tracedController")
@RequestMapping("/rest")
public class TracedController {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Value("${property.test0}")
  private String propertyTest0;

  @Value("${property.test1}")
  private String propertyTest1;

  @Value("${property.test2}")
  private String propertyTest2;

  @Value("${property.test3}")
  private String propertyTest3;

  @Value("${property.test4}")
  private String propertyTest4;

  @Value("${property.test5}")
  private String propertyTest5;

  @RequestMapping(value = "/blah", method = GET, produces = TEXT_PLAIN_VALUE)
  public String blah() {
    logger.info("in /blah");

    return "blah";
  }

  @RequestMapping(value = "/oops", method = GET, produces = TEXT_PLAIN_VALUE)
  public String oops() {
    logger.info("in /oops");

    throw new IllegalStateException("oops");
  }

  @RequestMapping(value = "/testProperty", method = GET, produces = TEXT_PLAIN_VALUE)
  public String testProperty() {
    logger.info("in /testProperty");

    return propertyTest0
        + "-" + propertyTest1
        + "-" + propertyTest2
        + "-" + propertyTest3
        + "-" + propertyTest4
        + "-" + propertyTest5;
  }
}
