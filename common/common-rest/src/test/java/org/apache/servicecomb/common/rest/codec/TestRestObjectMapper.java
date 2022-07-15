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

package org.apache.servicecomb.common.rest.codec;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.servicecomb.foundation.common.utils.RestObjectMapper;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

public class TestRestObjectMapper {

  @Test
  public void testFormateDate() throws Exception {
    RestObjectMapper mapper = new RestObjectMapper();
    // must read/write ISO 8061 dates
    Date date = mapper.readValue("\"2017-07-21T17:32:28Z\"".getBytes(), Date.class);
    String dateStr = mapper.writeValueAsString(date);
    Assertions.assertEquals(dateStr, "\"2017-07-21T17:32:28.000+00:00\"");

    date = mapper.readValue("\"2017-07-21T17:32:28.320+0100\"".getBytes(), Date.class);
    dateStr = mapper.writeValueAsString(date);
    Assertions.assertEquals(dateStr, "\"2017-07-21T16:32:28.320+00:00\""); // one hour later
  }

  @Test
  public void testAutoCloseSource() {
    Assertions.assertFalse(RestObjectMapperFactory.getRestObjectMapper().getFactory().isEnabled(Feature.AUTO_CLOSE_SOURCE));
  }

  @Test
  public void testDeserializationFeature() {
    Assertions.assertFalse(
        RestObjectMapperFactory.getRestObjectMapper().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
  }

  @Test
  public void testDefaultViewInclusionFeature() {
    Assertions.assertFalse(RestObjectMapperFactory.getRestObjectMapper().isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION));
  }

  @Test
  public void testJsonObjectWork() {
    JsonObject obj = new JsonObject();
    obj.put("name", "a");
    obj.put("desc", "b");
    PojoModel model = RestObjectMapperFactory.getRestObjectMapper()
        .convertValue(obj, TypeFactory.defaultInstance().constructType(PojoModel.class));
    Assertions.assertEquals("a", model.getName());
    Assertions.assertEquals("b", model.getDesc());

    RestObjectMapperFactory.setDefaultRestObjectMapper(new RestObjectMapper());
    model = RestObjectMapperFactory.getRestObjectMapper()
        .convertValue(obj, TypeFactory.defaultInstance().constructType(PojoModel.class));
    Assertions.assertEquals("a", model.getName());
    Assertions.assertEquals("b", model.getDesc());

    InputStream inputStream = new ByteArrayInputStream(new byte[0]);
    try {
      RestObjectMapperFactory.getRestObjectMapper().readValue(inputStream, PojoModel.class);
      Assertions.fail();
    } catch (MismatchedInputException e) {
      // right place, nothing to do.
    } catch (Exception e) {
      Assertions.fail();
    }
  }
}
