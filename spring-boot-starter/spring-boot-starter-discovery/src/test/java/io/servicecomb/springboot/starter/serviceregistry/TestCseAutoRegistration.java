package io.servicecomb.springboot.starter.serviceregistry;

import org.junit.Assert;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import org.junit.Test;
import org.mockito.Mockito;

public class TestCseAutoRegistration {

	@Test
	public void testRegistration() {
		ApplicationContext mockApplicationContext = Mockito.mock(ApplicationContext.class);
		Mockito.when(mockApplicationContext.getEnvironment()).thenReturn(Mockito.mock(Environment.class));
		Mockito.when(mockApplicationContext.getId()).thenReturn("test");

		CseAutoRegistration oCseAutoRegistration = CseAutoRegistration.registration(mockApplicationContext);
		Assert.assertEquals(oCseAutoRegistration.getService().getServiceId(), "test");
	}

	@Test
	public void testRegistrationBean() {
		ApplicationContext mockApplicationContext = Mockito.mock(ApplicationContext.class);
		Mockito.when(mockApplicationContext.getEnvironment()).thenReturn(Mockito.mock(Environment.class));
		Mockito.when(mockApplicationContext.getId()).thenReturn("test");

		CseAutoServiceRegistrationAutoConfiguration cseAutoServiceRegistrationAutoConfiguration = new CseAutoServiceRegistrationAutoConfiguration();

		CseAutoRegistration oCseAutoRegistration = cseAutoServiceRegistrationAutoConfiguration
				.cseRegistration(mockApplicationContext);
		Assert.assertEquals(oCseAutoRegistration.getService().getServiceId(), "test");
		Assert.assertEquals(oCseAutoRegistration.getInstanceId(), "test");
		Assert.assertNull(oCseAutoRegistration.getServiceId());
	}

}
