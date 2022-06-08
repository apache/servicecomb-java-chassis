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

package org.apache.servicecomb.foundation.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class JsonUtilsTest {

    @Test
    void testWriteUnicodeValueAsString() throws JsonProcessingException {
        String unicodeStr = "测试";
        Map<String, String> inMap = new HashMap<>();
        inMap.put("key", unicodeStr);

        Assertions.assertFalse(StringUtils.isAsciiPrintable(JsonUtils.writeValueAsString(inMap)));
        String jsonStr = JsonUtils.writeUnicodeValueAsString(inMap);
        Assertions.assertTrue(StringUtils.isAsciiPrintable(jsonStr));

        Map<String, String> outMap = JsonUtils.OBJ_MAPPER.readValue(jsonStr, new TypeReference<Map<String, String>>() {
        });
        Assertions.assertEquals(unicodeStr, outMap.get("key"));
    }
}
