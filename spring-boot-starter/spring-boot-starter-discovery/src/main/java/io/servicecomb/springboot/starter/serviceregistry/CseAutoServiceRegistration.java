package io.servicecomb.springboot.starter.serviceregistry;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

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
