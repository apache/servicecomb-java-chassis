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

package org.apache.servicecomb.swagger.generator.core.processor.response;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.servicecomb.swagger.generator.OperationGenerator;
import org.apache.servicecomb.swagger.generator.SwaggerGenerator;
import org.apache.servicecomb.swagger.jakarta.ModelConvertersAdapterJakarta;
import org.reactivestreams.Publisher;

import io.swagger.models.Model;
import jakarta.ws.rs.core.MediaType;

public class PublisherProcessor extends DefaultResponseTypeProcessor {
  protected static final List<String> EVENTS_PRODUCE = List.of(MediaType.SERVER_SENT_EVENTS);

  public PublisherProcessor() {
    extractActualType = true;
  }

  @Override
  public Type getProcessType() {
    return Publisher.class;
  }

  @Override
  public Model process(SwaggerGenerator swaggerGenerator, OperationGenerator operationGenerator,
      Type genericResponseType) {
    ModelConvertersAdapterJakarta.getInstance().addClassToSkip(Publisher.class.getName());
    operationGenerator.getOperation().produces(EVENTS_PRODUCE);
    return super.process(swaggerGenerator, operationGenerator, genericResponseType);
  }
}
