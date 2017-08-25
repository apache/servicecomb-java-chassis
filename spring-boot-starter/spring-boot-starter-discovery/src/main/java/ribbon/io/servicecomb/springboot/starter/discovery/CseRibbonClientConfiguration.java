/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ribbon.io.servicecomb.springboot.starter.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;

import io.servicecomb.springboot.starter.discovery.CseRoutesProperties;
import io.servicecomb.springboot.starter.discovery.ServiceCombServerList;

/**
 * Custom {@link org.springframework.cloud.netflix.ribbon.RibbonClient} configuration must not be
 * scanned by spring.
 *
 * @see <a href="http://cloud.spring.io/spring-cloud-static/Camden.SR4/#_customizing_the_ribbon_client">
 * Customizing the Ribbon Client </a>
 */
@Configuration
@ConditionalOnBean(ZuulProperties.class)
public class CseRibbonClientConfiguration {

  @Bean
  ServerList<Server> ribbonServerList(
      IClientConfig config,
      CseRoutesProperties cseRoutesProperties) {

    ServiceCombServerList serverList = new ServiceCombServerList(cseRoutesProperties);
    serverList.initWithNiwsConfig(config);
    return serverList;
  }
}
