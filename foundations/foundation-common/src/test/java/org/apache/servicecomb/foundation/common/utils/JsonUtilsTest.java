package org.apache.servicecomb.foundation.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JsonUtilsTest extends TestCase {

    @Test
    public void testWriteUnicodeValueAsString() throws JsonProcessingException {
        String unicodeStr = "测试";
        Map<String, String> inMap = new HashMap<>();
        inMap.put("key", unicodeStr);

        Assert.assertFalse(StringUtils.isAsciiPrintable(JsonUtils.writeValueAsString(inMap)));
        String jsonStr = JsonUtils.writeUnicodeValueAsString(inMap);
        Assert.assertTrue(StringUtils.isAsciiPrintable(jsonStr));

        Map<String, String> outMap = JsonUtils.OBJ_MAPPER.readValue(jsonStr, new TypeReference<Map<String, String>>() {
        });
        Assert.assertEquals(unicodeStr, outMap.get("key"));
    }
}