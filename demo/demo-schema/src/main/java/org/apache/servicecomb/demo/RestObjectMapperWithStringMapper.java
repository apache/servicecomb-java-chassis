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

package org.apache.servicecomb.demo;

import org.apache.commons.lang3.ClassUtils;
import org.apache.servicecomb.common.rest.codec.RestObjectMapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.vertx.core.json.JsonObject;

/**
 *  Demonstrate how to using String as raw type when using RestTemplate to invoke a service that use POJO. e.g.
 *  <p/>
 *  Provider: <p/>
 *  <code>
 *    public Response errorCodeWithHeader(MultiRequest request)
 *  </code>
 *   <p/>
 *  Consumer: <p/>
 *  <code>
 *    String stringRequest = "{\"key\":\"testValue\"}";
 *    template.postForEntity(url, stringRequest, MultiResponse200.class);
 *  </code>
 * <p/>
 *  <b>Caution:</b> json will convert String to object based on String constructor, using this feature will make default
 *  conversion change. You must write  convertValue to check possible types using.
 */
public class RestObjectMapperWithStringMapper extends RestObjectMapper {
  private static final long serialVersionUID = -8158869347066287575L;

  private static final JavaType STRING_JAVA_TYPE = TypeFactory.defaultInstance().constructType(String.class);

  @SuppressWarnings("deprecation")
  public RestObjectMapperWithStringMapper() {
    super();
  }

  @Override
  public <T> T convertValue(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
    if (String.class.isInstance(fromValue)
        && !String.class.equals(toValueType.getRawClass())
        && !java.util.Date.class.equals(toValueType.getRawClass())
        && !ClassUtils.isPrimitiveOrWrapper(toValueType.getRawClass())) {
      return super.convertValue(new JsonObject((String) fromValue), toValueType);
    }
    return super.convertValue(fromValue, toValueType);
  }
}
