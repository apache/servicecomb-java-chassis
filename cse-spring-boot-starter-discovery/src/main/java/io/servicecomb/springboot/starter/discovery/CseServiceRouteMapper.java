/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.springboot.starter.discovery;

import org.springframework.cloud.netflix.zuul.filters.discovery.ServiceRouteMapper;

public class CseServiceRouteMapper implements ServiceRouteMapper {
    @Override
    public String apply(String serviceId) {
       /* ServiceRegistryClient client = RegistryClientFactory.getRegistryClient();
        String appId = DynamicPropertyFactory.getInstance().getStringProperty("APPLICATION_ID", "default").get();
        String versionRule = DynamicPropertyFactory.getInstance().getStringProperty("service_description.version", "1.0.0").get();
        String cseServiceID = client.getMicroserviceId(appId , serviceId, versionRule);
        List<MicroserviceInstance> cseServices = client.getMicroserviceInstance(cseServiceID, cseServiceID);
        if(null != cseServices && !cseServices.isEmpty())
        {
        for(MicroserviceInstance instance:cseServices) 
        {
            List<String> eps=instance.getEndpoints();
            for(String ep:eps)
            {
                if(ep.startsWith("rest"))
                {
                    String path = ep.replace("rest", "http");
                    if(path.contains("?"))
                    {
                        path = path.split("\\?")[0];
                    }
                    serviceId = path+"/springmvctest";
                }
            }
        }
        }*/
        String[] service= serviceId.split("/");
//        return service[service.length-1];
        return "springmvctest";
    }
}
