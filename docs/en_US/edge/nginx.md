## 概念阐述

### **confd**

confd是一个轻量级的配置管理工具，源码地址：[https://github.com/kelseyhightower/confd](https://github.com/kelseyhightower/confd)，它可以将配置信息存储在etcd、consul、dynamodb、redis以及zookeeper等。confd定期会从这些存储节点pull最新的配置，然后重新加载服务，完成配置文件的更新。

### **Nginx**

Nginx \(engine x\)是一个高性能的HTTP和反向代理服务器，具有负载均衡的功能。详情请参考[http://www.nginx.cn/doc/](http://www.nginx.cn/doc/)。本小节介绍的服务主要使用到的是Nginx的http代理功能。

## 场景描述

本小节介绍的技术是使用nginx+confd做边缘服务，同时可以对接Java Chassis微服务框架中的服务中心，从服务中心中拉去服务信息通过confd动态更新nginx的配置。

使用nginx+confd动态反向代理的实现步骤可参考文章[http://www.cnblogs.com/Anker/p/6112022.html](http://www.cnblogs.com/Anker/p/6112022.html)，本节主要介绍confd如何对接Java Chassis框架的服务中心。

## 对接服务中心

本节介绍的技术核心在于如何使得confd获取到服务中心的服务信息，服务中心开放了以下接口供外部调用：

### **方法一：http调用**

服务中心开放http接口均需要添加租户头部信息：“X-Tenant-Name:tenantName”，tenameName为租户名，默认为default，例如"X-Tenant-Name:default"。

* 检查服务中心健康状态

  ```
   GET 127.0.0.1:30100/health
  ```

* 获取所有微服务信息

  ```
   GET 127.0.0.1:30100/registry/v3/microservices
  ```

* 获取指定id的微服务信息

> 1. 首先根据微服务信息获取serviceId
>
>    ```
>    GET 127.0.0.1:30100/registry/v3/existence?type=microservice&appId={appId}&serviceName={serviceName}&version={version}
>    ```
>
> 2. 根据上述接口返回的serviceId获取微服务完整信息
>
>    GET 127.0.0.1:30100/registry/v3/microservices/{serviceId}

* 获取指定微服务的所有实例信息

  ```
   GET 127.0.0.1:30100/registry/v3/microservices/{serviceId}/instances

   需要在header中添加："X-ConsumerId:{serviceId}"。
  ```

* 查找微服务实例信息

  ```
   GET 127.0.0.1:30100/registry/v3/instances?appId={appId}&serviceName={serviceName}&version={version}

   需要在header中添加: "X-ConsumerId:{serviceId}"。
  ```


#### 注意：在实际开发中请访问实际的service-center访问地址，并将上述url中{}的变量替换成具体值，http返回的数据均为json格式

### **方法二：使用servicecomb开源代码接口**

在开发微服务应用，只需要调用servicecomb框架代码中的工具类RegistryUtil.java中提供的接口，即可获取服务中心的信息，接口描述如下：

* 获取所有微服务信息  

  ```java
  List<Microservice> getAllMicroservices();
  ```

* 获取微服务唯一标识  

  ```java
  String getMicroserviceId(String appId, String microserviceName, String versionRule);
  ```

* 根据微服务唯一标识查询微服务静态信息  

  ```java
  Microservice getMicroservice(String microserviceId);
  ```

* 根据多个微服务唯一标识查询所有微服务实例信息  

  ```java
  List<MicroserviceInstance> getMicroserviceInstance(String consumerId, String providerId);
  ```

* 按照app+interface+version查询实例endpoints信息  

  ```java
  List<MicroserviceInstance> findServiceInstance(String consumerId, String appId, String serviceName,String versionRule);
  ```

通过上述http接口可获取到服务中心的微服务和其实例的信息，从而通过confd动态更新nginx配置。

