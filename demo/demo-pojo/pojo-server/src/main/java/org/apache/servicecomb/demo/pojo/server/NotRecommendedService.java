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

package org.apache.servicecomb.demo.pojo.server;

import java.util.List;
import java.util.Map;

import org.apache.servicecomb.demo.server.AbstractModel;
import org.apache.servicecomb.demo.server.NotRecommendedServiceInf;
import org.apache.servicecomb.demo.server.WrappedAbstractModel;
import org.apache.servicecomb.provider.pojo.RpcSchema;

@RpcSchema(schemaId = "NotRecommendedService", schemaInterface = NotRecommendedServiceInf.class)
public class NotRecommendedService implements NotRecommendedServiceInf {

  @Override
  public Map<Long, Long> longMap(Map<Long, Long> map) {
    return map;
  }

  @Override
  public List<AbstractModel> listAbstractModel(List<AbstractModel> listModel) {
    return listModel;
  }

  @Override
  public AbstractModel abstractModel(AbstractModel model) {
    return model;
  }

  @Override
  public Map<Long, AbstractModel> mapAbstractModel(Map<Long, AbstractModel> mapModel) {
    return mapModel;
  }

  @Override
  public WrappedAbstractModel wrappedAbstractModel(WrappedAbstractModel wrappedModel) {
    return wrappedModel;
  }
}
