## 概念阐述

微服务架构解决了很多单体应用带来的问题，但同时也需要我们付出额外的代价。由于网络的不稳定性带来的请求处理延迟就是代价之一。

在单体应用中，所有模块都在同一个进程中运行，所以并没有模块间互通的问题。但微服务架构中，服务间通过网络沟通，因此我们不得不处理和网络有关的 问题，例如：延迟、超时、网络分区等。

另外，随着业务的扩展服务增多，我们很难洞察数据如何在蛛网般复杂的服务结构中流转。我们如何才能有效的监控网络延迟并且可视化服务中的数据流转呢？

**分布式调用链追踪**用于有效地监控微服务的网络延时并可视化微服务中的数据流转。

## Zipkin

> [Zipkin](http://zipkin.io/)是一个分布式调用链追踪系统。 它能帮助用户收集时序数据用以定位微服务中的延迟问题，它同时管理追踪数据的收集 和查询。Zipkin 的设计基于 Google [Dapper paper](http://research.google.com/pubs/pub36356.html)。

ServiceComb 集成了 Zipkin 提供自动调用链追踪能力，如此一来用户只需要专注实现其业务需求。

## 使用步骤:

### 添加依赖

基于 ServiceComb Java Chassis 的微服务只需要添加如下依赖到 pom.xml：

```xml
<dependency>   
    <groupId>org.apache.servicecomb</groupId>
    <artifactId>handler-tracing-zipkin</artifactId>
</dependency>
```

如果微服务是基于 Spring Cloud + Zuul 的 API 网关，例如 workshop demo 中的 manager service ，我们还需要加入如下的额外依赖：

```xml
<dependency>    
    <groupId>org.apache.servicecomb</groupId>
    <artifactId>spring-cloud-zuul-zipkin</artifactId>
</dependency>
```

### 配置追踪处理和数据收集 {#配置追踪处理和数据收集}

在microservice.yaml文件中设置追踪处理器和数据收集服务地址

```yaml
  servicecomb: 
    handler: 
      chain: 
        Consumer: 
          default: tracing-consumer
        Provider: 
          default: tracing-provider
  servicecomb: 
    tracing: 
      collector: 
        address: http://zipkin.servicecomb.io:9411
```

就这样，在加了两个配置项，没改动一行代码的情况下，我们启动了基于 Zipkin 和 Java chassis 的分布式调用链追踪的功能。

**注意 **如果项目中的其他依赖也引入了 zipkin （例如 Spring Cloud），可能导致 zipkin 版本不一致而运行出错，这时需要在项目 pom 中声明 zipkin 版本。



