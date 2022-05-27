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

package org.apache.servicecomb.springboot.jaxrs.server;

import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.demo.RestObjectMapperWithStringMapper;
import org.apache.servicecomb.demo.RestObjectMapperWithStringMapperNotWriteNull;
import org.apache.servicecomb.springboot2.starter.EnableServiceComb;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EnableServiceComb
public class JaxrsServer {
  public static void main(final String[] args) throws Exception {
    RestObjectMapperFactory.setDefaultRestObjectMapper(new RestObjectMapperWithStringMapper());
    RestObjectMapperFactory.setConsumerWriterMapper(new RestObjectMapperWithStringMapperNotWriteNull());

    new SpringApplicationBuilder().sources(JaxrsServer.class).web(WebApplicationType.NONE).build().run(args);
  }
}
