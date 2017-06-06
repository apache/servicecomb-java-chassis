package io.servicecomb.springboot.starter.serviceregistry;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.servicecomb.springboot.starter.discovery.CseDiscoveryProperties;

@Configuration
@ConditionalOnBean(AutoServiceRegistrationProperties.class)
@AutoConfigureAfter(CseServiceRegistryAutoConfiguration.class)
public class CseAutoServiceRegistrationAutoConfiguration {

	@Bean
	private CseAutoServiceRegistration cseAutoServiceRegistration(CseServiceRegistry serviceRegistry,
			CseDiscoveryProperties properties, CseAutoRegistration registration) {
		return new CseAutoServiceRegistration(serviceRegistry, properties, registration);
	}

	@Bean
	private CseAutoRegistration cseRegistration(ApplicationContext applicationContext) {
		return CseAutoRegistration.registration(applicationContext);
	}

}
