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

package org.apache.servicecomb.provider.rest.common;

import java.util.List;

import org.apache.servicecomb.common.rest.RestConst;
import org.apache.servicecomb.core.provider.producer.AbstractProducerProvider;
import org.apache.servicecomb.core.provider.producer.ProducerMeta;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;

public class RestProducerProvider extends AbstractProducerProvider {
  @Override
  public String getName() {
    return RestConst.REST;
  }

  @Override
  public List<ProducerMeta> init() {
    // for some UT case, there is no spring context
    if (BeanUtils.getContext() == null) {
      return null;
    }

    RestProducers restProducers = BeanUtils.getContext().getBean(RestProducers.class);
    return restProducers.getProducerMetaList();
  }
}
