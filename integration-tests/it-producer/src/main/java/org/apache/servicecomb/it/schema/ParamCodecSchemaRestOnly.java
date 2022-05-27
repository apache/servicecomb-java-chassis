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

package org.apache.servicecomb.it.schema;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.servicecomb.foundation.test.scaffolding.model.Media;
import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "paramCodecRestOnly")
@Path("/v1/paramCodecRestOnly")
public class ParamCodecSchemaRestOnly {
  /**
   * Test special enum name tagged by {@link com.fasterxml.jackson.annotation.JsonProperty}.
   * Special name not supported by ProtoBuffer
   */
  @Path("enum/enumSpecialName")
  @POST
  public Media enumSpecialName(Media media) {
    return media;
  }
}
