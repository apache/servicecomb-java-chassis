## 概念阐述

### API Gateway：

API Gateway是一个服务器，也可以说是进入系统的唯一节点。API Gateway封装内部系统的架构，并且提供API给各个客户端。

### Zuul

Zuul是Netflix的基于JVM的路由器和服务器端负载均衡器，可以使用Zuul进行以下操作：

* 认证
* 洞察
* 压力测试
* 金丝雀测试
* 动态路由
* 服务迁移
* 负载脱落
* 安全
* 静态相响应处理
* 主动/被动流量管理

本小节主要介绍在SpringBoot应用中使用Zuul做API Gateway。关于Zuul的详细功能介绍请参考文档[路由器和过滤器：Zuul](https://springcloud.cc/spring-cloud-dalston.html#_router_and_filter_zuul)。

## 场景描述

Zuul做API Gateway，即建立一个Zuul Proxy应用，在该Proxy应用中统一定义所有的微服务访问入口，通过使用不同的前缀\(stripped\)来区分各个微服务。本小节通过建立一个ZuulProxy SpringBoot应用来演示Zuul的API Gateway功能。

## 启动Zuul Proxy

本节介绍如何启动一个zuul proxy应用作为API Gateway。步骤如下：

* **步骤 1**在pom文件中添加依赖：

```xml
<dependency> 
  <groupId>org.springframework.cloud</groupId>  
  <artifactId>spring-cloud-starter-zuul</artifactId> 
</dependency>
<dependency> 
  <groupId>org.springframework.cloud</groupId>  
  <artifactId>spring-cloud-starter-ribbon</artifactId> 
</dependency><dependency> 
  <groupId>org.springframework.cloud</groupId>  
  <artifactId>spring-cloud-starter-hystrix</artifactId> 
</dependency>
<dependency> 
  <groupId>org.apache.servicecomb</groupId>  
  <artifactId>spring-boot-starter-discovery</artifactId> 
</dependency>
```

* **步骤 2**在SpringBoot主类添加注解：

```java
@SpringBootApplication
@EnableServiceComb
@EnableZuulProxy//新增注解
public class ZuulMain{
    public static void main(String[] args) throws Exception{
　　    SpringApplication.run(ZuulMain.class, args);
    }
}
```

* **步骤 3**在application.yml文件中定义路由策略：

```yaml
server: 
  port: 8754 #api gateway服务端口
zuul: 
  routes: #路由策略
    discoveryServer: /myServer/** #路由规则
```

红色的配置项表示可以根据实际开发环境进行配置。关于zuul.routers的路由策略的详细定义规则，请参考官方文献：[路由器和过滤器：Zuul](https://springcloud.cc/spring-cloud-dalston.html#_router_and_filter_zuul)，可更细粒度地对路由进行控制。

* **步骤 4**在microservice.yaml定义微服务属性：

```yaml
APPLICATION_ID: discoverytest #服务ID
service_description: 
  name: discoveryGateway #服务名称
  version: 0.0.2 #服务版本号
servicecomb: 
  service: 
    registry: 
      address:  http://127.0.0.1:30100            #服务注册中心地址
 rest: 
   address: 0.0.0.0:8082                         #微服务端口，可不写
```

本小节所有服务使用的都是本地的服务中心，具体的服务中心启动流程请参考[启动本地服务中心](#li2337491491436)

* **步骤 5　**Run ZuulMain Application

## 使用Zuul Proxy

在使用zuul做的API Gateway前，首先要启动在zuul.routers中定义的微服务提供者。

开发服务提供者，开按流程请参考3 开发服务提供者。在微服务microservice.yaml文件中需注意以下两点：

* APPLICATION\_ID需要于zuul proxy中定义的保持一致。

* service\_description.name需要于zuul.routers中相对应。

示例如下：

```yaml
APPLICATION_ID: discoverytest #与zuul proxy一致
service_description: 
  name: discoveryServer #服务名称，与zuul.routers对应
  version: 0.0.2
servicecomb: 
  service: 
    registry: 
      address: http://127.0.0.1:30100                #服务注册中心地址
rest: 
  address: 0.0.0.0:8080
```

API Gateway的访问入口为：[http://127.0.0.1:8754](http://127.0.0.1:8754)，所有在zuul.routers中定义的服务都可通过这个访问入口进行代理访问，访问规则如下：

[http://127.0.0.1:8754/myServer/\*\*\*](http://127.0.0.1:8754/myServer/***)

这表示，Http调用[http://127.0.0.1:8754/myServer/\*\*\*](http://127.0.0.1:8754/myServer/***)，会转到discoveryServer服务（例如："/myServer/101"跳转到discoveryServer 服务下的"/101"）

> 如果在服务中心同时存在多个discoveryServer服务\(版本不同\),zuul默认采用Ribbon策略对请求进行转发。



