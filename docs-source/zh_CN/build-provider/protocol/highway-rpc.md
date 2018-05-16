## 概念阐述

Highway是ServiceComb的高性能私有协议，用户可在有特殊性能需求的场景下选用。

## 配置说明

使用Highway网络通道需要在maven pom文件中添加如下依赖：

```xml
 <dependency>
     <groupId>org.apache.servicecomb</groupId>
     <artifactId>transport-highway</artifactId>
 </dependency>
```

Highway通道在microservice.yaml文件中的配置项如下表所示：

表1-1Highway配置项说明

| 配置项 | 默认值 | 取值范围 | 是否必选 | 含义 | 注意 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| servicecomb.highway.address | 0.0.0.0:7070 | - | 否 | 服务监听地址 | - |
| servicecomb.highway.server.thread-count | 1 | - | 否 | 服务端网络线程个数 | - |
| servicecomb.highway.client.thread-count | 1 | - | 否 | 客户端网络线程个数 | - |
| servicecomb.request.timeout | 30000 | - | 否 | 请求超时时间 | 同REST over Vertx的配置 |
| servicecomb.references.\[服务名\].transport | rest |  | 否 | 访问的transport类型 | 同REST over Vertx的配置 |
| servicecomb.references.\[服务名\].version-rule | latest | - | 否 | 访问实例的版本号 | 同REST over Vertx的配置 |

## 示例代码

microservice.yaml文件中的配置示例：

```yaml
servicecomb:
  highway:
    address: 0.0.0.0:7070
```



