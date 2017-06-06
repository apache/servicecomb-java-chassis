package io.servicecomb.springboot.starter.serviceregistry;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.ApplicationContext;

import io.servicecomb.serviceregistry.api.registry.Microservice;

public class CseAutoRegistration extends CseRegistration {

	public CseAutoRegistration(Microservice service) {
		super(service);

	}

	public static CseAutoRegistration registration(ApplicationContext context) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(context.getEnvironment());
		Microservice microservice = new Microservice();
		microservice.setAppId(propertyResolver.getProperty("spring.application.name", "application"));
		microservice.setServiceId(context.getId());
		return new CseAutoRegistration(microservice);
	}

}
