package org.apache.servicecomb.router.constom;

import org.apache.servicecomb.loadbalance.ServiceCombServer;
import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.apache.servicecomb.serviceregistry.api.registry.Microservice;

public class ServiceCombCanaryDistributer extends
    AbstractRouterDistributor<ServiceCombServer, Microservice> {

    public ServiceCombCanaryDistributer() {
        init(server -> MicroserviceCache.getInstance()
                        .getService(server.getInstance().getServiceId()),
                Microservice::getVersion,
                Microservice::getServiceName,
                Microservice::getProperties);
    }

}
