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
package org.apache.servicecomb.zeroconfig;

import org.apache.servicecomb.provider.rest.common.RestSchema;

import org.apache.servicecomb.zeroconfig.client.ClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@RestSchema(schemaId = "schemaContentEndpoint")
@RequestMapping(path = "/schemaEndpoint")
public class SchemaContentEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaContentEndpoint.class);
    // each service self-expose this endpoint for others(consumers) to retrieve the schema content
    @RequestMapping(path = "/schemas", produces = MediaType.TEXT_PLAIN, method = RequestMethod.GET )
    public String getSchemaEndpoint(@RequestParam(name = "schemaId") String schemaId) {
        Map<String, String> schemaMap = ClientUtil.microserviceSelf.getSchemaMap();
        if (!schemaMap.isEmpty() && schemaMap.containsKey(schemaId)){
           return schemaMap.get(schemaId);
        }
        LOGGER.warn("schemaId: {} doesn't exist:", schemaId);
        return null;
    }
}