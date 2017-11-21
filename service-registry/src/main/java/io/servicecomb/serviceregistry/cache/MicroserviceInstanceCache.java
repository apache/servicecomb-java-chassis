package io.servicecomb.serviceregistry.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;

public class MicroserviceInstanceCache {

	private static final Logger logger = LoggerFactory.getLogger(MicroserviceInstanceCache.class);

	private static Cache<String, MicroserviceInstance> instances = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterAccess(30, TimeUnit.MINUTES).build();

	public static MicroserviceInstance getOrCreate(String serviceId, String instanceId) {
		try {
			String key = String.format("%s@%s", serviceId, instanceId);
			return instances.get(key, new Callable<MicroserviceInstance>() {

				@Override
				public MicroserviceInstance call() throws Exception {
					return getMicroserviceInstanceFromSC(serviceId, instanceId);
				}

			});
		} catch (ExecutionException e) {
			logger.error("get microservice from cache failed:" +  String.format("%s@%s", serviceId, instanceId));
			return null;
		}
	}

	private static MicroserviceInstance getMicroserviceInstanceFromSC(String serviceId, String instanceId) {
		return RegistryUtils.getServiceRegistryClient().findServiceInstance(serviceId, instanceId);
	}

}
