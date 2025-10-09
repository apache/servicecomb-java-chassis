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

package org.apache.servicecomb.registry.lightweight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import io.vertx.core.json.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class MessageTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String toLinuxPrettyJson(Object value) {
    return Json.encodePrettily(value)
        .replaceAll("\r\n", "\n");
  }

  @Test
  void should_encode_register_type() {
    Message<RegisterRequest> msg = Message.of(MessageType.REGISTER, new RegisterRequest());
    String actualJson = toLinuxPrettyJson(msg);
    String expectedJson =
        "{\n" +
        "  \"type\" : \"REGISTER\",\n" +
        "  \"body\" : {\n" +
        "    \"appId\" : null,\n" +
        "    \"serviceId\" : null,\n" +
        "    \"crossApp\" : false,\n" +
        "    \"schemasSummary\" : null,\n" +
        "    \"instanceId\" : null,\n" +
        "    \"status\" : null,\n" +
        "    \"endpoints\" : null\n" +
        "  }\n" +
        "}";

    try {
      JsonNode actual   = MAPPER.readTree(actualJson);
      JsonNode expected = MAPPER.readTree(expectedJson);

      assertThat(actual).isEqualTo(expected);

      assertThat(actual.path("type").asText()).isEqualTo("REGISTER");
      assertThat(actual.at("/body/crossApp").asBoolean()).isFalse();

    } catch (Exception e) {
      fail("Failed to parse/compare JSON: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
    }
  }

  @Test
  void should_decode_register_type() {
    String json = Json.encode(Message.of(MessageType.REGISTER, new RegisterRequest()));
    Message<?> message = Json.decodeValue(json, Message.class);

    assertThat(message.getBody()).isInstanceOf(RegisterRequest.class);
  }

  @Test
  void should_encode_unregister_type() {
    Message<UnregisterRequest> msg = Message.of(MessageType.UNREGISTER, new UnregisterRequest());
    String actualJson = toLinuxPrettyJson(msg);
    String expectedJson = "{\n"
        + "  \"type\" : \"UNREGISTER\",\n"
        + "  \"body\" : {\n"
        + "    \"serviceId\" : null,\n"
        + "    \"instanceId\" : null\n"
        + "  }\n"
        + "}";

    try {
      JsonNode actual   = MAPPER.readTree(actualJson);
      JsonNode expected = MAPPER.readTree(expectedJson);

      assertEquals(expected, actual, "JSON mismatch");

      assertEquals("UNREGISTER", actual.path("type").asText());
      assertTrue(actual.path("body").has("serviceId"));
      assertTrue(actual.path("body").has("instanceId"));
    } catch (Exception e) {
      fail("Failed to parse/compare JSON: " + e.getMessage(), e);
    }
  }

  @Test
  void should_decode_unregister_type() {
    String json = Json.encode(Message.of(MessageType.UNREGISTER, new UnregisterRequest()));
    Message<?> message = Json.decodeValue(json, Message.class);

    assertThat(message.getBody()).isInstanceOf(UnregisterRequest.class);
  }
}
