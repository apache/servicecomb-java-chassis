## 配置说明

REST over Vertx通信通道对应使用standalone部署运行模式，可直接通过main函数拉起。main函数中需要初始化日志和加载服务配置，代码如下：

```java
public class MainServer {
    public static void main(String[] args) throws Exception {
        Log4jUtils.init();//日志初始化
        BeanUtils.init(); // Spring bean初始化
    }
}
```

使用REST over Vertx网络通道需要在maven pom文件中添加如下依赖：

```xml
<dependency>
    <groupId>org.apache.servicecomb</groupId>
    <artifactId>transport-rest-vertx</artifactId>
</dependency>
```

REST over Vertx通道在microservice.yaml文件中有以下配置项：

表1-1 REST over Vertx配置项说明

| 配置项 | 默认值 | 取值范围 | 是否必选 | 含义 | 注意 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| servicecomb.rest.address | 0.0.0.0:8080 | - | 否 | 服务监听地址 | 仅服务提供者需要配置 |
| servicecomb.rest.server.thread-count | 1 | - | 否 | 服务端线程数 | 仅服务提供者需要配置 |
| servicecomb.rest.server.connection.idleTimeoutInSeconds | 60 | - | 否 | 服务端连接闲置超时时间 | 闲置连接会被回收 |
| servicecomb.rest.client.thread-count | 1 | - | 否 | 客户端网络线程数 | 仅服务消费者需要配置 |
| servicecomb.rest.client.connection.maxPoolSize | 5 | - | 否 | 每个连接池的最大连接数 | 连接数=网络线程数\*连接池个数\*连接池连接数 |
| servicecomb.rest.client.connection.idleTimeoutInSeconds | 30 | - | 否 | 连接闲置时间 | 闲置连接会被回收 |
| servicecomb.rest.client.connection.keepAlive | true | - | 否 | 是否使用长连接 |  |
| servicecomb.request.timeout | 30000 | - | 否 | 请求超时时间 |  |
| servicecomb.references.\[服务名\].transport | rest |  | 否 | 访问的transport类型 | 仅服务消费者需要配置 |
| servicecomb.references.\[服务名\].version-rule | latest | - | 否 | 访问实例的版本号 | 仅服务消费者需要配置支持latest，1.0.0+，1.0.0-2.0.2，精确版本。详细参考服务中心的接口描述。 |

## 示例代码

microservice.yaml文件中的配置示例：

```yaml
servicecomb:
  rest:
    address: 0.0.0.0:8080
    thread-count: 1
  references:
    hello:
      transport: rest
      version-rule: 0.0.1
```



