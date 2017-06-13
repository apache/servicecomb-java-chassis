package io.servicecomb.springboot.starter.serviceregistry;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.serviceregistry.api.registry.Microservice;

public class TestCseServiceRegistry {

	@Test
	public void testCseServiceRegistry() {
		Microservice microservice = new Microservice();
		microservice.setAppId("test");
		microservice.setServiceName("test");
		CseRegistration cseRegistration = new CseRegistration(Mockito.mock(Microservice.class));

		// TODO test Registering the service
	}

	@Test
	public void testCseServiceRegistrybean() {

		CseServiceRegistryAutoConfiguration cseServiceRegistryAutoConfiguration = new CseServiceRegistryAutoConfiguration();
		CseServiceRegistry cseServiceRegistry = cseServiceRegistryAutoConfiguration.cseServiceRegistry();
		Assert.assertNotNull(cseServiceRegistry);
	}
}
