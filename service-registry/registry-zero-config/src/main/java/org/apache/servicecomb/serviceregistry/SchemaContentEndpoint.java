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
package org.apache.servicecomb.serviceregistry;

import org.apache.servicecomb.provider.rest.common.RestSchema;

import org.apache.servicecomb.serviceregistry.client.ClientUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static org.apache.servicecomb.serviceregistry.ZeroConfigRegistryConstants.*;

@RestSchema(schemaId = SCHEMA_CONTENT_ENDPOINT)
@RequestMapping(path = SCHEMA_CONTENT_ENDPOINT_BASE_PATH)
public class SchemaContentEndpoint {
    // each service self-expose this endpoint for others(consumers) to retrieve the schema content
    @RequestMapping(path = SCHEMA_CONTENT_ENDPOINT_SUBPATH, produces = MediaType.TEXT_PLAIN, method = RequestMethod.POST )
    public String getSchemaEndpoint(@RequestParam(name = SCHEMA_CONTENT_ENDPOINT_QUERY_KEYWORD) String schemaId) {
        Map<String, String> schemaMap = ClientUtil.microserviceSelf.getSchemaMap();
        return schemaMap != null ? schemaMap.computeIfPresent(schemaId,  (key, schemaContent) -> { return schemaContent;}) : null;
    }
}