这个模块主要提供 servicestage 部署环境变量和配置项的映射，通过这种方式可以简化配置。 

servicestage 存在环境、应用、组件等概念，这些内容都是在部署的时候输入，可以将这些内容用于微服务的配置信息。 

|概念|环境变量|微服务配置项|
| :--- | :--- | :--- |
|应用|CAS_APPLICATION_NAME|servicecomb.service.application|
|组件|CAS_COMPONENT_NAME|servicecomb.service.name|
|组件版本|CAS_INSTANCE_VERSION|servicecomb.service.version|

部署的过程还会注入环境里面的资源信息。

|概念|环境变量|微服务配置项|
| :--- | :--- | :--- |
|服务/配置中心地址（逻辑多租，APIG场景）| PAAS_CSE_ENDPOINT | servicecomb.service.registry.address<br/>servicecomb.config.client.serverUri|
|服务中心地址 | PAAS_CSE_SC_ENDPOINT | servicecomb.service.registry.address |
|配置中心地址 | PAAS_CSE_CC_ENDPOINT | servicecomb.config.client.serverUri |
|项目（区域） | PAAS_PROJECT_NAME | servicecomb.credentials.project |


