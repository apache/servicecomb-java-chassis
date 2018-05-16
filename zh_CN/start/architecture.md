## ![](/start/architecture.png)框架概述

## **主要设计意图**

1.编程模型和通信模型分离，不同的编程模型可以灵活组合不同的通信模型。应用开发者在开发阶段只关注接口开发，部署阶段灵活切换通信方式；支持legacy系统的切换，legacy系统只需要修改服务发布的配置文件（或者annotation），而不需要修改代码。

现阶段支持SpringMVC、JAX-RS和透明RPC三种开发方式。

2.内建API-first支持。通过契约规范化微服务开发，实现跨语言的通信，并支持配套的软件工具链（契约生成代码、代码生成契约等）开发，构建完整的开发生态。

3.定义了常用的微服务运行模型，将微服务从发现到交互过程中的各种容错手段都封装起来。该运行模型支持自定义和扩展。

## **模块说明**

| 类型 | artifact id | 是否可选 | 功能说明 |
| :--- | :--- | :--- | :--- |
| 编程模型 | provider-pojo | 是 | 提供RPC开发模式 |
| 编程模型 | provider-jaxrs | 是 | 提供JAX RS开发模式 |
| 编程模型 | provider-springmvc | 是 | 提供Spring MVC开发模式 |
| 通信模型 | transport-rest-vertx | 是 | 运行于HTTP之上的开发框架，不依赖WEB容器，应用打包为可执行jar |
| 通信模型 | transport-rest-servlet | 是 | 运行于WEB容器的开发框架，应用打包为war包 |
| 通信模型 | transport-highway | 是 | 提供高性能的私有通信协议，仅用于java之间互通。 |
| 运行模型 | handler-loadbalance | 是 | 负载均衡模块。提供各种路由策略和配置方法。一般客户端使用。 |
| 运行模型 | handler-bizkeeper | 是 | 和服务治理相关的功能，比如隔离、熔断、容错。 |
| 运行模型 | handler-tracing | 是 | 调用链跟踪模块，对接监控系统，吐出打点数据。 |



