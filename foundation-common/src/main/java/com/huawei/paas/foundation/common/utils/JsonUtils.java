/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.paas.foundation.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author   
 * @version  [版本号, 2016年11月22日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public final class JsonUtils {
    /**
     * ObjectMapper
     */
    public static final ObjectMapper OBJ_MAPPER;

    static {
        //如果待转换的字符串比较随机，这个缓存很容易就填满，会导致同步清空缓存，对性能有影响
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonFactory.Feature.INTERN_FIELD_NAMES, false);
        OBJ_MAPPER = new ObjectMapper(jsonFactory);
    }

    //    static
    //    {
    //        //设置反序列化时忽略json字符串中存在而Java对象实际没有的属性
    //        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //    }

    private JsonUtils() {
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param src src
     * @param valueType valueType
     * @param <T> T
     * @return T
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws Exception Exception
     */
    public static <T> T readValue(byte[] src,
            Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
        return OBJ_MAPPER.readValue(src, valueType);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param is is
     * @param valueType valueType
     * @param <T> T
     * @return T
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws Exception Exception
     */
    public static <T> T readValue(InputStream is,
            Class<T> valueType) throws JsonParseException, JsonMappingException, IOException {
        return OBJ_MAPPER.readValue(is, valueType);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param is is
     * @param valueType valueType
     * @param <T> T
     * @return T
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @throws Exception Exception
     */
    public static <T> T readValue(InputStream is,
            JavaType valueType) throws JsonParseException, JsonMappingException, IOException {
        return OBJ_MAPPER.readValue(is, valueType);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param value value
     * @return byte[]
     * @throws JsonProcessingException
     * @throws Exception Exception
     */
    public static byte[] writeValueAsBytes(Object value) throws JsonProcessingException {
        return OBJ_MAPPER.writeValueAsBytes(value);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param value value
     * @return String
     * @throws JsonProcessingException
     * @throws Exception Exception
     */
    public static String writeValueAsString(Object value) throws JsonProcessingException {
        return OBJ_MAPPER.writeValueAsString(value);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param fromValue fromValue
     * @param toValueType toValueType
     * @param <T> T
     * @return T
     * @throws Exception Exception
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return OBJ_MAPPER.convertValue(fromValue, toValueType);
    }

    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param out out
     * @param value value
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     * @throws Exception Exception
     */
    public static void writeValue(OutputStream out,
            Object value) throws JsonGenerationException, JsonMappingException, IOException {
        OBJ_MAPPER.writeValue(out, value);
    }

    // TODO：移走
    /**
     * Converts the local time to UTC time
     * @param date local date
     * @return UTC Date
     */
    public static Date getUTCDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return new Date(cal.getTimeInMillis());
    }
}
