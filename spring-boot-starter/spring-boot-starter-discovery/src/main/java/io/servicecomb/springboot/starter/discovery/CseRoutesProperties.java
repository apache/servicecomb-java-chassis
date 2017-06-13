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
package io.servicecomb.springboot.starter.discovery;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;

import io.servicecomb.core.provider.consumer.ConsumerProviderManager;
import io.servicecomb.core.provider.consumer.ReferenceConfig;

import com.netflix.config.DynamicPropertyFactory;

public final class CseRoutesProperties {

	@Autowired
	private ConsumerProviderManager consumerProviderManager;

	@Autowired(required = true)
	private ZuulProperties zuulProperties;

	private final Map<String, String> appServiceMap = new HashMap<String, String>();;

	@PostConstruct
	private void loadZuulRoutes() {
		Map<String, ZuulRoute> zuulrouteMap = zuulProperties.getRoutes();
		for (String key : zuulrouteMap.keySet()) {
			appServiceMap.put(key, zuulrouteMap.get(key).getServiceId());
		}
	};

	public String getServiceName(String appID) {
		String serviceName = appServiceMap.get(appID);
		if (null == serviceName || serviceName.trim().isEmpty()) {
			serviceName = DynamicPropertyFactory.getInstance().getStringProperty("service_description.name", "default")
					.get();
		}
		return serviceName;
	}

	public String getVersionRule(String serviceName) {
		ReferenceConfig referenceConfig = consumerProviderManager.getReferenceConfig(serviceName);
		return referenceConfig.getMicroserviceVersionRule();
	}

	public String getAppID() {
		return DynamicPropertyFactory.getInstance().getStringProperty("APPLICATION_ID", "default").get();
	}

}
