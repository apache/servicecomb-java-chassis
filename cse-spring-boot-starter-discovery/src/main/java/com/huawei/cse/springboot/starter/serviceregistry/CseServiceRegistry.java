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

package com.huawei.cse.springboot.starter.serviceregistry;

import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huawei.paas.cse.serviceregistry.api.registry.Microservice;
import com.huawei.paas.cse.serviceregistry.client.ServiceRegistryClient;

public class CseServiceRegistry implements ServiceRegistry<CseRegistration> {

	private static final Logger log = LoggerFactory.getLogger(CseServiceRegistry.class);

	private final ServiceRegistryClient client;

	public CseServiceRegistry(ServiceRegistryClient client) {
		this.client = client;
		try {
			this.client.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void register(CseRegistration reg) {

		String serviceId = client.registerMicroservice(reg.getService());
		// Watch
		client.watch(serviceId, changedEvent -> {
			if (changedEvent.succeeded()) {
				log.info("{} {}/{} changed", changedEvent.result().getAction(),
						changedEvent.result().getKey().getServiceName(), changedEvent.result().getKey().getVersion());
				for (String s : changedEvent.result().getInstance().getEndpoints()) {
					log.info("  -> {}", s);
				}
			} else {
				log.error("", changedEvent.cause());
			}
		}, open -> {
		}, close -> {
		});

	}

	@Override
	public void close() {

	}

	@Override
	public void setStatus(CseRegistration registration, String status) {
		// client.updateInstanceProperties(registration.getService(),
		// registration.getInstanceId(), status);
		// TODO Expose an Api in ServiceRegistryClient to change the status of
		// the MicroService

	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getStatus(CseRegistration registration) {

		Microservice oMicroservice = client.getMicroservice(registration.getServiceId());
		return oMicroservice.getStatus();

	}

	@Override
	public void deregister(CseRegistration arg0) {
		// TODO Auto-generated method stub

		client.unregisterMicroserviceInstance(arg0.getServiceId(), arg0.getInstanceId());
		log.info("MicroService unregistered successfully" + arg0.getService().getServiceName());
	}
}
