package io.servicecomb.serviceregistry.api.response;

import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class MicroInstanceResponse {

	private MicroserviceInstance instance;

	public MicroserviceInstance getInstance() {
		return instance;
	}

	public void setInstance(MicroserviceInstance instance) {
		this.instance = instance;
	}
	
	
}
