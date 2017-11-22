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

/**
 * 微服务实例缓存 key为：serviceId@instanceId 缓存limit：1000 缓存老化策略：30分钟没有访问就过期。
 *
 */
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
					logger.debug("get microservice instance from SC");
					return getMicroserviceInstanceFromSC(serviceId, instanceId);
				}

			});
		} catch (ExecutionException e) {
			logger.error("get microservice from cache failed:" + String.format("%s@%s", serviceId, instanceId));
			return null;
		}
	}

	private static MicroserviceInstance getMicroserviceInstanceFromSC(String serviceId, String instanceId) {
		return RegistryUtils.getServiceRegistryClient().findServiceInstance(serviceId, instanceId);
	}

}
