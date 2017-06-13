package io.servicecomb.springboot.starter.serviceregistry;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.servicecomb.springboot.starter.discovery.CseDiscoveryProperties;

public class TestCseAutoServiceRegistration {

	@Test
	public void testCseAutoServiceRegistration() {
		CseDiscoveryProperties cseDiscoveryProperties = Mockito.mock(CseDiscoveryProperties.class);
		Mockito.when(cseDiscoveryProperties.getPort()).thenReturn("8090");
		CseServiceRegistry cseServiceRegistry = Mockito.mock(CseServiceRegistry.class);
		CseAutoRegistration cseAutoRegistration = Mockito.mock(CseAutoRegistration.class);

		CseAutoServiceRegistration cseAutoServiceRegistration = new CseAutoServiceRegistration(cseServiceRegistry,
				cseDiscoveryProperties, cseAutoRegistration);
		Assert.assertEquals(cseAutoServiceRegistration.getRegistration(), cseAutoRegistration);
		Assert.assertEquals(cseAutoServiceRegistration.getManagementRegistration(), cseAutoRegistration);
		Assert.assertEquals(cseAutoServiceRegistration.getConfiguredPort(), 8090);
		Assert.assertEquals(cseAutoServiceRegistration.getConfiguration(), cseDiscoveryProperties);
		Assert.assertEquals(cseAutoServiceRegistration.isEnabled(), false);

	}

	@Test
	public void testCseAutoServiceRegistrationBean() {
		CseDiscoveryProperties cseDiscoveryProperties = Mockito.mock(CseDiscoveryProperties.class);
		Mockito.when(cseDiscoveryProperties.getPort()).thenReturn("8090");
		CseServiceRegistry cseServiceRegistry = Mockito.mock(CseServiceRegistry.class);
		CseAutoRegistration cseAutoRegistration = Mockito.mock(CseAutoRegistration.class);

		CseAutoServiceRegistrationAutoConfiguration cseAutoServiceRegistrationAutoConfiguration = new CseAutoServiceRegistrationAutoConfiguration();

		CseAutoServiceRegistration cseAutoServiceRegistration = cseAutoServiceRegistrationAutoConfiguration
				.cseAutoServiceRegistration(cseServiceRegistry, cseDiscoveryProperties, cseAutoRegistration);
		Assert.assertEquals(cseAutoServiceRegistration.getRegistration(), cseAutoRegistration);
		Assert.assertEquals(cseAutoServiceRegistration.getManagementRegistration(), cseAutoRegistration);
		Assert.assertEquals(cseAutoServiceRegistration.getConfiguredPort(), 8090);
		Assert.assertEquals(cseAutoServiceRegistration.getConfiguration(), cseDiscoveryProperties);
		Assert.assertEquals(cseAutoServiceRegistration.isEnabled(), false);

	}

}
