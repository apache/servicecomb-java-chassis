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

package org.apache.servicecomb.common.rest.codec.header;

import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import org.apache.servicecomb.common.rest.codec.RestClientRequest;
import org.apache.servicecomb.common.rest.codec.RestObjectMapperFactory;
import org.apache.servicecomb.common.rest.codec.param.HeaderProcessorCreator.HeaderProcessor;
import org.apache.servicecomb.swagger.invocation.exception.CommonExceptionData;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

public abstract class HeaderCodecWithDelimiter implements HeaderCodec {
  private final String name;

  private final String joinDelimiter;

  private final String splitDelimiter;

  public HeaderCodecWithDelimiter(String name, String joinDelimiter, String splitDelimiter) {
    this.name = name;
    this.joinDelimiter = joinDelimiter;
    this.splitDelimiter = splitDelimiter;
  }


  @Override
  public String getCodecName() {
    return name;
  }

  @Override
  public void encode(RestClientRequest clientRequest, String name, Object value) throws Exception {
    if (null == value) {
      // if value is empty, header should not be set to clientRequest to avoid NullPointerException in Netty.
      return;
    }
    if (!(value instanceof Collection<?>)) {
      throw new InvocationException(Status.BAD_REQUEST,
          new CommonExceptionData("Array type of header should be Collection"));
    }
    clientRequest.putHeader(name, join((Collection<?>) value));
  }

  protected String join(Collection<?> values) throws Exception {
    StringJoiner joiner = new StringJoiner(joinDelimiter);
    for (Object value : values) {
      String strValue = RestObjectMapperFactory.getConsumerWriterMapper().convertToString(value);
      joiner.add(strValue);
    }

    return joiner.toString();
  }

  @Override
  public Object decode(HeaderProcessor processor, HttpServletRequest request) {
    String headerValues = request.getHeader(processor.getParameterPath());
    if (headerValues == null) {
      headerValues = (String) processor.checkRequiredAndDefaultValue();
    }

    return processor.convertValue(Arrays.asList(headerValues.split(splitDelimiter)), processor.getTargetType());
  }
}
