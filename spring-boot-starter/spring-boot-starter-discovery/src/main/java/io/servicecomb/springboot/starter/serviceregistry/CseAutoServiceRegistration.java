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
package io.servicecomb.springboot.starter.serviceregistry;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;

import io.servicecomb.springboot.starter.discovery.CseDiscoveryProperties;

public class CseAutoServiceRegistration extends AbstractAutoServiceRegistration<CseRegistration> {

	private CseDiscoveryProperties cseDiscoveryProperties;

	private CseAutoRegistration cseAutoRegistration;

	protected CseAutoServiceRegistration(CseServiceRegistry serviceRegistry, CseDiscoveryProperties properties,
			CseAutoRegistration registration) {
		super(serviceRegistry);
		this.cseDiscoveryProperties = properties;
		this.cseAutoRegistration = registration;
	}

	@Override
	protected CseRegistration getRegistration() {
		return this.cseAutoRegistration;
	}

	@Override
	protected CseRegistration getManagementRegistration() {
		return this.cseAutoRegistration;
	}

	@Override
	protected int getConfiguredPort() {
		return Integer.parseInt(this.cseDiscoveryProperties.getPort());
	}

	@Override
	protected void setConfiguredPort(int port) {
		this.cseDiscoveryProperties.setHost(String.valueOf(port));

	}

	@Override
	protected Object getConfiguration() {
		return this.cseDiscoveryProperties;
	}

	@Override
	protected boolean isEnabled() {
		return false;
	}

}
