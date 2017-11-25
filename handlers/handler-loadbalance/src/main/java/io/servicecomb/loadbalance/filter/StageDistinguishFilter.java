package io.servicecomb.loadbalance.filter;

import com.netflix.loadbalancer.Server;
import io.servicecomb.loadbalance.CseServer;
import io.servicecomb.loadbalance.ServerListFilterExt;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstance;
import io.servicecomb.serviceregistry.api.registry.MicroserviceInstanceStatus;

import java.util.ArrayList;
import java.util.List;

public class StageDistinguishFilter implements ServerListFilterExt {
    @Override
    public List<Server> getFilteredListOfServers(List<Server> servers) {

        List<Server> filteredServers = new ArrayList<Server>();
        for (Server server : servers) {
            if (allowVisit(server)) {
                filteredServers.add(server);
            }
        }
        return filteredServers;
    }

    private boolean allowVisit(Server server) {
        MicroserviceInstance serInstance = ((CseServer) server).getInstance();
        return MicroserviceInstanceStatus.UP.equals(serInstance.getStatus()) && RegistryUtils.getMicroserviceInstance().getEnvironment().equals(serInstance.getEnvironment());
    }
}
