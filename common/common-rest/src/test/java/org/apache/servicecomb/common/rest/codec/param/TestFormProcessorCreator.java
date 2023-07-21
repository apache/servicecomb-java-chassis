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

package org.apache.servicecomb.common.rest.codec.param;

import java.util.HashMap;

import org.apache.servicecomb.common.rest.codec.param.FormProcessorCreator.FormProcessor;
import org.apache.servicecomb.swagger.generator.SwaggerConst;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TestFormProcessorCreator {
  @Test
  public void testCreate() {
    ParamValueProcessorCreator creator =
        ParamValueProcessorCreatorManager.INSTANCE.findValue(FormProcessorCreator.PARAMTYPE);
    RequestBody p = new RequestBody();
    p.setContent(new Content());
    p.getContent().addMediaType(SwaggerConst.FORM_MEDIA_TYPE, new MediaType());
    p.getContent().get(SwaggerConst.FORM_MEDIA_TYPE).setSchema(new Schema());
    p.getContent().get(SwaggerConst.FORM_MEDIA_TYPE).getSchema().setProperties(new HashMap<>());

    ParamValueProcessor processor = creator.create("p1", p, String.class);

    Assertions.assertEquals(FormProcessor.class, processor.getClass());
  }
}
