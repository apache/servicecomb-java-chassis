本章节主要描述微服务发布到华为公有云上。上云配置的基本原则是：只需要对microservice.yaml进行适当的配置，以及在pom中添加额外的依赖，就可以使用相关的功能。

# 一键式配置

公有云版本提供了一键式简化配置的方式，让基于开源版本开发的应用快速切换为云上应用，直接使用公有云提供的灰度发布、服务治理等功能。

* 增加依赖关系\(pom.xml\)

```xml
<dependency> 
  <groupId>com.huawei.paas.cse</groupId>  
  <artifactId>cse-solution-service-engine</artifactId> 
</dependency>
```

开发者只需要配置对cse-solution-service-engine依赖，就完成了公有云的所有配置。这个依赖关系主要给开发者做了如下事情：

* 引入相关依赖的组件

* 增加默认配置项。默认配置包含了处理链、负载均衡策略等。

可以通过查看pom内容，以及这个jar包里面的microservice.yaml文件查看引入的组件和增加的配置项。在下面的章节中，详细解释上云增加的组件以及他们的作用，让开发者更加深入的了解各种技术细节。

## 连接服务中心

### 功能描述

服务中心实现注册和发现，在FusionStage/ServiceStage查看微服务目录，都需要微服务连接上服务中心。

### 配置参考

* 增加依赖关系\(pom.xml\)

如果连接服务中心使用https协议/AK/SK认证，该jar依赖必选。如果是http协议，并不包含token验证，则无需引入。

```xml
<dependency> 
  <groupId>com.huawei.paas.cse</groupId>  
  <artifactId>foundation-auth</artifactId> 
</dependency>
```

* 配置项\(microservice.yaml）

```xml
servicecomb:
 service:
  registry:
   address: https://servicecomb.cn-north-1.myhwclouds.com:443    #根据实际地址配置服务中心地址
```

该配置项配置了服务中心的地址。其中，address可以在公有云“工具和案例”目录下查到对应的服务中心地址，修改协议（http/https）、主机名（可能使用域名）和端口号。

## 连接配置中心

### 功能描述

配置中心实现配置下发，连接配置中心是使用治理、灰度发布等功能的前台。

### 配置参考

* 增加依赖关系\(pom.xml\)

如果连接配置中心使用https协议/AK/SK认证，该jar依赖必选。如果是http协议，并不包含token验证，则无需引入。

```xml
<dependency> 
  <groupId>com.huawei.paas.cse</groupId>  
  <artifactId>foundation-auth</artifactId> 
</dependency>
```

连接配置中心该jar必选

```xml
<dependency> 
  <groupId>com.huawei.paas.cse</groupId>  
  <artifactId>foundation-config-cc</artifactId> 
</dependency>
```

* 启用配置\(microservice.yaml）

```yaml
servicecomb:
 config:
  client:
   serverUri: https://servicecomb.cn-north-1.myhwclouds.com:443
```

该配置项配置了配置中心的地址。其中，address可以在公有云“工具和案例”目录下查到对应的配置中心地址，修改协议（http/https）、主机名（可能使用域名）和端口号。

## 使用服务治理

### 功能描述

服务治理主要涉及“隔离”、“熔断”、“容错”、“限流”、“负载均衡”等。

### 配置参考

配置项\(microservice.yaml）

需要增加下面治理相关的handler，才能在从配置中心实时获取治理数据。

```yaml
servicecomb:
 handler:
  chain:
   Provider:
    default: bizkeeper-provider,qps-flowcontrol-provider
   Consumer:
    default: bizkeeper-consumer,loadbalance,qps-flowcontrol-consumer
```

## 使用故障注入

### 功能描述

故障注入主要提供了延时、错误两种类型故障。

### 配置参考

配置项\(microservice.yaml）

需要增加下面治理相关的handler。

```yaml
servicecomb:
 handler:
  chain:
   Consumer:
    default: loadbalance,fault-injection-consumer
```

## 使用灰度发布

### 功能描述

该功能对应于微服务目录灰度发布功能。管理员可以通过下发规则，对服务进行灰度发布管理。

### 配置参考

* 增加依赖关系\(pom.xml\)

引入必要的jar包。

```xml
<dependency> 
  <groupId>com.huawei.paas.cse</groupId>  
  <artifactId>cse-handler-cloud-extension</artifactId>
</dependency>
```

* 在Consumer端配置负载均衡\(microservice.yaml）

在负载均衡模块启用了灰度发布的filter。

```
servicecomb:
 loadbalance:
  serverListFilters: darklaunch
  serverListFilter:
    darklaunch:
      className: com.huawei.paas.darklaunch.DarklaunchServerListFilter
```

## 使用调用链

### 功能描述

华为云提供了业务无侵入的埋点功能APM。只需要通过华为云部署容器应用，并选择启用调用链监控功能，即可使用调用链服务。

## 微服务运行数据上报

### 功能描述

微服务可以将自己的运行数据上报给Dashboard服务，在公有云上查看仪表盘数据、分布式事务数据等。该章节是描述如何启用微服务数据上报功能。

### 配置参考

* 增加依赖关系\(pom.xml\)

引入必要的jar包。

```
<dependency>
　　<groupId>com.huawei.paas.cse</groupId>
　　<artifactId>cse-handler-cloud-extension</artifactId>
</dependency>
```

* 配置handler

仪表盘数据依赖于两个handler，一个bizkeeper-provider（客户端为bizkeeper-consumer），一个perf-stats，所以对应的pom依赖需要先引入。

```
servicecomb:  
  handler:
    chain:
      Provider:
        default: bizkeeper-provider,perf-stats,tracing-provider,sla-provider
      Consumer:
        default: bizkeeper-consumer,loadbalance,perf-stats,tracing-consumer,sla-consumer
```

* 配置上报monitor地址

TenantLB\_ADDRESS为共有云租户管理面接入地址，默认是100.125.1.34。

```
servicecomb:
  service:
    registry:
      address: https://${TenantLB_ADDRESS}:30100
  monitor:
    client:
      serverUri: https://${TenantLB_ADDRESS}:30109
```

## 分布式事务: TCC

### 功能描述

主要实现基于TCC协议的分布式服务间的最终一致性方案，保障一般场景下的应用一致性需求，并可以在FusionStage/ServiceStage分布式事务界面查看事务详细信息。

### 配置参考

* 增加依赖关系\(pom.xml\)

引入必要的jar包

```
<dependency>
　　<groupId>com.huawei.paas.cse</groupId>
　　<artifactId>cse-handler-tcc</artifactId>
</dependency>
```

* 配置项参考

需要增加下面事务相关的handler，才能在从配置中心实时获取治理数据。

```
servicecomb:
 handler:
  chain:
   Provider:
    default: tcc-client,bizkeeper-provider
   Consumer:
    default: tcc-server,bizkeeper-consumer,loadbalance
```



