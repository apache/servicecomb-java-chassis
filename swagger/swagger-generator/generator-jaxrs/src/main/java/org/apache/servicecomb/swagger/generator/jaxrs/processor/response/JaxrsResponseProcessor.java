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
package org.apache.servicecomb.swagger.generator.jaxrs.processor.response;

import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.generator.core.processor.response.DefaultResponseTypeProcessor;

public class JaxrsResponseProcessor extends DefaultResponseTypeProcessor {
  @Override
  public Class<?> getProcessType() {
    return Response.class;
  }

  @Override
  public Type extractResponseType(Type genericResponseType) {
    return null;
  }

  @Override
  public Type extractResponseType(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      Type genericResponseType) {
    // Response can not express respone type
    // if produces is text，then can assume to be string, otherwise can only throw exception
    List<String> produces = operationGenerator.getOperation().getProduces();
    if (produces == null) {
      produces = swaggerGenerator.getSwagger().getProduces();
    }
    if (produces != null) {
      if (produces.contains(MediaType.TEXT_PLAIN)) {
        return String.class;
      }
    }

    throw new IllegalStateException("Use ApiOperation or ApiResponses to declare response type");
  }
}
