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
package org.apache.servicecomb.swagger.invocation.response.producer;

import org.apache.servicecomb.swagger.invocation.Response;
import org.apache.servicecomb.swagger.invocation.sse.SseEventResponseEntity;
import org.reactivestreams.Publisher;

import io.reactivex.rxjava3.core.Flowable;
import jakarta.ws.rs.core.Response.StatusType;

public class PublisherProducerResponseMapper implements ProducerResponseMapper {
  private final boolean shouldConstructEntity;

  public PublisherProducerResponseMapper(boolean shouldConstructEntity) {
    this.shouldConstructEntity = shouldConstructEntity;
  }

  @Override
  public Response mapResponse(StatusType status, Object result) {
    // Unified Flowable conversion to prevent internal typecasting exceptions.
    final Flowable<?> flowableResult = result instanceof Flowable ?
            (Flowable<?>) result : Flowable.fromPublisher(((Publisher<?>) result));
    if (shouldConstructEntity) {
      Flowable<SseEventResponseEntity<?>> responseEntity = flowableResult
          .map(obj -> new SseEventResponseEntity<>().data(obj));
      return Response.create(status, responseEntity);
    }
    return Response.create(status, flowableResult);
  }
}
