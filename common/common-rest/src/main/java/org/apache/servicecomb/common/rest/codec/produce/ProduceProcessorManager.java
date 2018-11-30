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

package org.apache.servicecomb.common.rest.codec.produce;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.servicecomb.foundation.common.RegisterManager;
import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;

public final class ProduceProcessorManager extends RegisterManager<String, ProduceProcessor> {
  private static final List<ProduceProcessor> produceProcessor =
      SPIServiceUtils.getSortedService(ProduceProcessor.class);

  private static final String NAME = "produce processor mgr";

  public static final String DEFAULT_TYPE = MediaType.APPLICATION_JSON;

  public static final ProduceProcessorManager INSTANCE = new ProduceProcessorManager();

  public static final ProduceProcessor JSON_PROCESSOR =
      SPIServiceUtils.getTargetService(ProduceProcessor.class, ProduceJsonProcessor.class);

  public static final ProduceProcessor PLAIN_PROCESSOR =
      SPIServiceUtils.getTargetService(ProduceProcessor.class, ProduceTextPlainProcessor.class);

  public static final ProduceProcessor DEFAULT_PROCESSOR = JSON_PROCESSOR;

  private ProduceProcessorManager() {
    super(NAME);
    Set<String> set = new HashSet<>();
    produceProcessor.forEach(processor -> {
      if (set.add(processor.getName())) {
        register(processor.getName(), processor);
      }
    });
  }
}
