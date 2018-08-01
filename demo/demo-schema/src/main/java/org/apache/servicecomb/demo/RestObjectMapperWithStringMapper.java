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

import java.io.IOException;

import org.apache.servicecomb.common.rest.codec.RestObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.JavaType;

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
  private static final long serialVersionUID = 4279371572149490568L;

  private static Logger LOGGER = LoggerFactory.getLogger(RestObjectMapperWithStringMapper.class);

  public RestObjectMapperWithStringMapper() {
    super();
  }

  @Override
  public <T> T convertValue(Object fromValue, JavaType toValueType) throws IllegalArgumentException {
    if (String.class.isInstance(fromValue)
        && !BeanUtils.isSimpleValueType(toValueType.getRawClass())) {
      try {
        return super.readValue((String) fromValue, toValueType);
      } catch (IOException e) {
        LOGGER.error("Failed to convert value for {}.", e.getMessage());
      }
    }
    return super.convertValue(fromValue, toValueType);
  }
}
