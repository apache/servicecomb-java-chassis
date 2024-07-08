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

package org.apache.servicecomb.samples;

import java.util.List;

import org.apache.servicecomb.demo.api.IHeaderParamWithListSchema;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.provider.rest.common.RestSchema;

@RestSchema(schemaId = "ConsumerHeaderParamWithListSchema", schemaInterface = IHeaderParamWithListSchema.class)
public class ConsumerHeaderParamWithListSchema implements IHeaderParamWithListSchema {
  @RpcReference(microserviceName = "provider", schemaId = "HeaderParamWithListSchema")
  private IHeaderParamWithListSchema provider;

  @Override
  public String headerListDefault(List<String> headerList) {
    return provider.headerListDefault(headerList);
  }

  @Override
  public String headerListCSV(List<String> headerList) {
    return provider.headerListCSV(headerList);
  }

  @Override
  public String headerListMULTI(List<String> headerList) {
    return provider.headerListMULTI(headerList);
  }

  @Override
  public String headerListSSV(List<String> headerList) {
    return provider.headerListSSV(headerList);
  }

  @Override
  public String headerListPIPES(List<String> headerList) {
    return provider.headerListPIPES(headerList);
  }
}
