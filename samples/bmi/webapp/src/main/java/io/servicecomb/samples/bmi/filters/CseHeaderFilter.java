/*
 *  Copyright 2017 Huawei Technologies Co., Ltd
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.servicecomb.samples.bmi.filters;

import static io.servicecomb.core.Const.CSE_CONTEXT;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import io.servicecomb.config.ConfigUtil;
import io.servicecomb.config.archaius.sources.MicroserviceConfigLoader;
import io.servicecomb.foundation.common.utils.JsonUtils;
import io.servicecomb.serviceregistry.definition.MicroserviceDefinition;

@Component
public class CseHeaderFilter extends ZuulFilter {
  @Override
  public String filterType() {
    return "pre";
  }

  @Override
  public int filterOrder() {
    return 0;
  }

  @Override
  public boolean shouldFilter() {
    return true;
  }

  @Override
  public Object run() {
    RequestContext ctx = RequestContext.getCurrentContext();
    MicroserviceConfigLoader loader = ConfigUtil.getMicroserviceConfigLoader();
    MicroserviceDefinition microserviceDefinition = new MicroserviceDefinition(loader.getConfigModels());
    ctx.addZuulRequestHeader("x-cse-src-microservice", microserviceDefinition.getMicroserviceName());

    try {
      ctx.addZuulRequestHeader(CSE_CONTEXT, JsonUtils.writeValueAsString(ctx.getZuulRequestHeaders()));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to write request headers as json to " + CSE_CONTEXT, e);
    }
    return null;
  }
}
