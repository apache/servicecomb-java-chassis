针对Spring Boot、Spring Cloud，提供了如下几个组件：

* spring-boot-starter-configuration

接入配置中心。当需要在Spring Boot、Spring Cloud应用中，使用配置中心作为动态配置管理工具的时候，需要依赖。



* spring-boot-starter-registry

接入服务中心。当需要在Spring Boot、Spring Cloud应用中，使用服务中心作为服务注册、发现管理工具的时候，需要依赖。



* spring-boot-starter-discovery

适配Spring Cloud的DiscoveryClient接口。当在Spring Cloud中使用@EnableDiscoveryClient时，需要依赖。



* spring-boot-starter-provider

在Spring Boot中通过@EnableServiceComb启用java chassis的核心功能。这个功能可以用于“JAVA应用方式”和”Web开发方式“。 在”Web开发方式”中，通过spring.main.web-environment=false禁用了Web环境。因此，这个模块主要是解决”JAVA应用方式“的问题。



* spring-boot-starter-transport

在Spring Boot中通过@EnableServiceComb启用java chassis的核心功能，并启用java chassis的RestServlet。用于” Web开发方式“。

