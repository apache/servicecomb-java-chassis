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
package org.apache.servicecomb.foundation.vertx.stream;

import java.util.Objects;

import io.vertx.core.spi.PumpFactory;
import io.vertx.core.streams.Pump;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

public class PumpFactoryImpl implements PumpFactory {
  @Override
  public <T> Pump pump(ReadStream<T> rs, WriteStream<T> ws) {
    Objects.requireNonNull(rs);
    Objects.requireNonNull(ws);
    return new PumpImpl<>(rs, ws);
  }

  @Override
  public <T> Pump pump(ReadStream<T> rs, WriteStream<T> ws, int writeQueueMaxSize) {
    Objects.requireNonNull(rs);
    Objects.requireNonNull(ws);
    return new PumpImpl<>(rs, ws, writeQueueMaxSize);
  }
}